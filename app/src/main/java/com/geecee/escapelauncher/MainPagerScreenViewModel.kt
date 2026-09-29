package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppResult
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppUseCase
import com.geecee.escapelauncher.core.domain.launcher.GetIsDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.IsManagedProfileSupportedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileExistsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.system.LockScreenUseCase
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainPagerScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val onboardingRepository: OnboardingRepository,
    private val screenTimeSettingsRepository: ScreenTimeSettingsRepository,
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

    suspend fun animatedGoToMainPage() {
        val targetPage = getMainPageIndex()

        if (pagerState.currentPage == targetPage && pagerState.currentPageOffsetFraction == 0f) {
            return
        }

        // Immediately after a fling-driven gesture ends on a page's content, the pager can briefly
        // still be mid-mutation settling onto the current page, or about to start doing so. Starting
        // our own animation before that finishes (or right as it starts) gets it interrupted, which
        // visibly plays out as a completed hop back to the source page followed by a second
        // animation to the target - a flicker through multiple pages instead of one clean
        // transition. Waiting for any in-flight scroll to clear, plus a brief grace period for one
        // that's just about to start, avoids that race in practice; the retry below is only a
        // last-resort safety net for the rare case a competing mutation still wins regardless.
        snapshotFlow { pagerState.isScrollInProgress }.first { !it }
        delay(80)

        repeat(3) { attempt ->
            if (pagerState.currentPage == targetPage && pagerState.currentPageOffsetFraction == 0f) {
                return
            }
            try {
                pagerState.animateScrollToPage(
                    targetPage,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
                return
            } catch (e: CancellationException) {
                // MutationInterruptedException (thrown when a competing mutation pre-empts this
                // one) is an internal, non-public CancellationException subtype, so it's identified
                // by name here. Any other cancellation (e.g. this coroutine's own scope ending)
                // rethrows immediately instead of being retried.
                if (e::class.simpleName != "MutationInterruptedException" || attempt == 2) throw e
                delay(80)
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
