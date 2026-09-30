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

    suspend fun animatedGoToMainPage() {
        animateToPageAtElevatedPriority(getMainPageIndex())
    }

    suspend fun animatedGoToSearchPage() {
        animateToPageAtElevatedPriority(getAppsListPageIndex())
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
