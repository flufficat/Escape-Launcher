package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppResult
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppUseCase
import com.geecee.escapelauncher.core.domain.launcher.GetIsDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.IsManagedProfileSupportedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileExistsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.SearchSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.system.LockScreenUseCase
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.SearchGestureDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// How far off-screen the search page's vertical slide starts/ends from, for both the gesture
// entry animation and its mirrored close. Not derived from actual screen height since it only
// needs to safely exceed it.
private const val VERTICAL_SLIDE_DISTANCE_PX = 3000f

@HiltViewModel
class MainPagerScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val onboardingRepository: OnboardingRepository,
    private val screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    private val searchSettingsRepository: SearchSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    private val tryOpenAppUseCase: TryOpenAppUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val managedProfileExistsUseCase: ManagedProfileExistsUseCase,
    private val isManagedProfileSupportedUseCase: IsManagedProfileSupportedUseCase,
    private val getIsDefaultLauncherUseCase: GetIsDefaultLauncherUseCase,
    private val lockScreenUseCase: LockScreenUseCase
) : AndroidViewModel(context as Application) {
    fun managedProfileExists(type: ManagedProfileType): Boolean = managedProfileExistsUseCase(type)
    fun isManagedProfileSupported(type: ManagedProfileType): Boolean = isManagedProfileSupportedUseCase(type)
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            onboardingRepository.setFirstTimeHelp(value)
        }
    }

    val hideScreenTimePage = screenTimeSettingsRepository.hideScreenTimePage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    val searchGestureDirection = searchSettingsRepository.searchGestureDirection.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchGestureDirection.valueOf(DefaultSettings.SEARCH_GESTURE_DIRECTION)
    )
    val doubleTapToLock = launcherBehaviorRepository.doubleTapToLock
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    // Which gesture direction (if any) the search page was most recently entered from, so its
    // content can animate in from - and later back out to - that same edge (see
    // openSearchPageFromGesture/animatedGoToMainPage below). Null means it was reached the
    // normal way (a horizontal pager drag), which needs no extra animation since the pager's
    // own drag already visually handles that transition both ways. Cleared whenever the pager
    // actually settles on the main page, from any cause (our own animation or a manual drag),
    // so a stale direction from a previous session can never leak into an unrelated close.
    private val _searchEntryDirection = MutableStateFlow<SearchGestureDirection?>(null)
    val searchEntryDirection: StateFlow<SearchGestureDirection?> = _searchEntryDirection.asStateFlow()

    // Drives the appsListPageIndex branch's vertical slide offset (see MainPagerScreen). Lives
    // here rather than as Composable-scoped `remember` state so the same close animation can be
    // driven from animatedGoToMainPage() regardless of which call site triggers it.
    private val _verticalOffsetPx = MutableStateFlow(0f)
    val verticalOffsetPx: StateFlow<Float> = _verticalOffsetPx.asStateFlow()

    val isHiddenPrivateSpace = launcherBehaviorRepository.hidePrivateSpace

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    fun updateLauncherStatus() {
        _isDefaultLauncher.value = getIsDefaultLauncherUseCase()
    }

    fun lockScreen() {
        lockScreenUseCase()
    }

    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    var showOpenChallenge = mutableStateOf(false)

    val interactionSource = MutableInteractionSource()

    val appsListScrollState = LazyListState()

    val pagerState = PagerState(
        currentPage = if (hideScreenTimePage.value) 0 else 1,
        currentPageOffsetFraction = 0f
    ) {
        if (hideScreenTimePage.value) 2 else 3
    }

    private fun getMainPageIndex(): Int {
        return if (hideScreenTimePage.value) 0 else 1
    }

    suspend fun goToMainPage() {
        pagerState.scrollToPage(getMainPageIndex())
    }

    private fun getAppsListPageIndex(): Int {
        return if (hideScreenTimePage.value) 1 else 2
    }

    // Mirrors whatever got the user onto the search page in the first place: if it was a
    // vertical gesture, this plays the same slide back out the way it came in, rather than
    // always closing with the pager's horizontal animation regardless of entry direction.
    suspend fun animatedGoToMainPage() {
        val entryDirection = _searchEntryDirection.value
        android.util.Log.d("CloseDebug", "animatedGoToMainPage: entryDirection=$entryDirection currentPage=${pagerState.currentPage}")
        if (entryDirection == SearchGestureDirection.UP || entryDirection == SearchGestureDirection.DOWN) {
            val exitTarget = if (entryDirection == SearchGestureDirection.UP) VERTICAL_SLIDE_DISTANCE_PX else -VERTICAL_SLIDE_DISTANCE_PX
            val anim = Animatable(_verticalOffsetPx.value)
            anim.animateTo(exitTarget, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)) {
                _verticalOffsetPx.value = value
            }
            // The search text field is still focused at this point, and this jump is what
            // disposes it as the page changes - that disposal triggers the same OS-level
            // ATTACH_NEW_INPUT-driven competing mutation described on animateToPageAtElevatedPriority
            // below, which would otherwise hijack a plain scrollToPage() here into visibly playing
            // the pager's own default (horizontal) snap animation instead of just landing silently.
            jumpToPageAtElevatedPriority(getMainPageIndex())
            _verticalOffsetPx.value = 0f
            _searchEntryDirection.value = null
        } else {
            animateToPageAtElevatedPriority(getMainPageIndex())
        }
    }

    suspend fun animatedGoToSearchPage() {
        animateToPageAtElevatedPriority(getAppsListPageIndex())
    }

    // Used when search is opened via the Up/Down home-screen gesture: the pager itself jumps to
    // the search page instantly with no animation of its own (it only ever animates horizontally,
    // which would look wrong for a vertical gesture), and the search page's content plays its own
    // vertical slide-in animation from the given direction instead (see MainPagerScreen).
    suspend fun openSearchPageFromGesture(direction: SearchGestureDirection) {
        android.util.Log.d("CloseDebug", "openSearchPageFromGesture: direction=$direction")
        _searchEntryDirection.value = direction
        _verticalOffsetPx.value = if (direction == SearchGestureDirection.UP) VERTICAL_SLIDE_DISTANCE_PX else -VERTICAL_SLIDE_DISTANCE_PX
        pagerState.scrollToPage(getAppsListPageIndex())

        val anim = Animatable(_verticalOffsetPx.value)
        anim.animateTo(0f, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)) {
            _verticalOffsetPx.value = value
        }
    }

    // Disposing a focused text field (e.g. the search box) as its page scrolls out of the pager's
    // retained window triggers an OS-level input-focus reattachment cycle (ATTACH_NEW_INPUT),
    // which in turn drives a competing, default-priority mutation on this same pager (almost
    // certainly a window-inset-driven scroll adjustment). animateScrollToPage() runs at
    // MutatePriority.Default, so that competing mutation cancels it mid-flight, and it plays
    // out visibly as a completed hop to the target page followed by a snap back to the source
    // page before a second animation finally lands on the target - a flicker instead of one
    // clean transition. Running our own scroll at PreventUserInput (the highest priority)
    // makes it immune to that: nothing at Default or UserInput priority can interrupt it.
    // animateScrollToPage() can't be reused here directly - nesting it inside an outer
    // scroll(PreventUserInput) block doesn't inherit that priority, it starts a second,
    // separately-prioritized mutation that immediately self-conflicts - so the animation is
    // driven manually instead: an Animatable stepping through the same page-index range,
    // applying each frame's delta via the elevated-priority ScrollScope's scrollBy.
    // Same elevated-priority protection as animateToPageAtElevatedPriority below, but for an
    // instant jump rather than an animated scroll - used when the visual motion has already
    // been handled some other way (the vertical slide Animatable) and this call just needs to
    // land on the target page without being hijacked by a competing mutation.
    private suspend fun jumpToPageAtElevatedPriority(targetPage: Int) {
        val pageSizeWithSpacing = (pagerState.layoutInfo.pageSize + pagerState.layoutInfo.pageSpacing).toFloat()
        android.util.Log.d("CloseDebug", "jumpToPageAtElevatedPriority: targetPage=$targetPage pageSizeWithSpacing=$pageSizeWithSpacing currentPage=${pagerState.currentPage} offsetFraction=${pagerState.currentPageOffsetFraction}")
        if (pageSizeWithSpacing <= 0f) {
            android.util.Log.d("CloseDebug", "jumpToPageAtElevatedPriority: FALLBACK plain scrollToPage used")
            pagerState.scrollToPage(targetPage)
            return
        }

        pagerState.scroll(MutatePriority.PreventUserInput) {
            val startValue =
                (pagerState.currentPage + pagerState.currentPageOffsetFraction) * pageSizeWithSpacing
            val targetValue = targetPage * pageSizeWithSpacing
            scrollBy(targetValue - startValue)
        }
        android.util.Log.d("CloseDebug", "jumpToPageAtElevatedPriority: AFTER currentPage=${pagerState.currentPage} offsetFraction=${pagerState.currentPageOffsetFraction}")
    }

    private suspend fun animateToPageAtElevatedPriority(targetPage: Int) {
        if (pagerState.currentPage == targetPage && pagerState.currentPageOffsetFraction == 0f) {
            return
        }

        val pageSizeWithSpacing = (pagerState.layoutInfo.pageSize + pagerState.layoutInfo.pageSpacing).toFloat()
        if (pageSizeWithSpacing <= 0f) {
            // Not laid out yet; fall back to the standard animation rather than divide by zero.
            pagerState.animateScrollToPage(
                targetPage,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            return
        }

        pagerState.scroll(MutatePriority.PreventUserInput) {
            val scope = this
            val startValue =
                (pagerState.currentPage + pagerState.currentPageOffsetFraction) * pageSizeWithSpacing
            val targetValue = targetPage * pageSizeWithSpacing
            var previousValue = startValue
            val anim = Animatable(startValue)
            anim.animateTo(
                targetValue,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            ) {
                scope.scrollBy(value - previousValue)
                previousValue = value
            }
        }
    }

    init {
        updateLauncherStatus()

        // The pager is constructed before DataStore has emitted, so `hideScreenTimePage` is still
        // its placeholder value at that point. Re-anchor the pager on the main page once the real
        // value is known, and again whenever the setting is toggled (the page indices shift).
        viewModelScope.launch {
            screenTimeSettingsRepository.hideScreenTimePage
                .distinctUntilChanged()
                .collect { hide -> pagerState.scrollToPage(if (hide) 0 else 1) }
        }

        // Self-healing: if the user leaves the search page by a plain horizontal drag instead of
        // going through animatedGoToMainPage() (e.g. dragging back rather than pressing back),
        // that path never clears searchEntryDirection/verticalOffsetPx on its own. Settling on
        // the main page from any cause resets both, so a stale vertical entry direction can never
        // leak into a later, unrelated close.
        viewModelScope.launch {
            snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
                .collect { (page, scrolling) ->
                    if (!scrolling && page == getMainPageIndex()) {
                        if (_searchEntryDirection.value != null) {
                            android.util.Log.d("CloseDebug", "self-healing watcher clearing entryDirection=${_searchEntryDirection.value} page=$page")
                        }
                        _searchEntryDirection.value = null
                        _verticalOffsetPx.value = 0f
                    }
                }
        }
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
    }

    /**
     * Logic to handle what happens when an app is launched
     */
    fun onAppLaunched(app: InstalledApp) {
        updateSelectedApp(app)
        viewModelScope.launch {
            delay(500.milliseconds)
            goToMainPage()
        }
    }

    /**
     * High-level function to open an app, handling challenge checks asynchronously
     */
    fun openApp(
        app: InstalledApp,
        overrideChallenge: Boolean = false,
        onAppOpened: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            when (tryOpenAppUseCase(app.packageName, overrideChallenge)) {
                TryOpenAppResult.ShowChallenge -> {
                    showOpenChallenge.value = true
                    updateSelectedApp(app)
                }
                TryOpenAppResult.Launch -> {
                    if (launchAppUseCase(app, onAppOpened)) {
                        onAppLaunched(app)

                        // At the end of an open challenge countdown, it runs this openApp function again with overrideChallenge set to true so this is used to hide the challenge ui
                        if (overrideChallenge) {
                            showOpenChallenge.value = false
                        }
                    }
                }
            }
        }
    }
}
