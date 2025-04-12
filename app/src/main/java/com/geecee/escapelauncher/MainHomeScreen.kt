package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escapelauncher.ui.theme.AppTheme
import com.geecee.escapelauncher.ui.theme.EscapeTheme
import com.geecee.escapelauncher.ui.theme.offLightScheme
import com.geecee.escapelauncher.ui.views.HomeScreenModel
import com.geecee.escapelauncher.ui.views.HomeScreenModelFactory
import com.geecee.escapelauncher.ui.views.HomeScreenPageManager
import com.geecee.escapelauncher.ui.views.Onboarding
import com.geecee.escapelauncher.ui.views.Settings
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.animateSplashScreen
import com.geecee.escapelauncher.utils.AppUtils.configureAnalytics
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.PrivateSpaceStateReceiver
import com.geecee.escapelauncher.utils.ScreenOffReceiver
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getIntSetting
import com.geecee.escapelauncher.utils.managers.ChallengesManager
import com.geecee.escapelauncher.utils.managers.FavoriteAppsManager
import com.geecee.escapelauncher.utils.managers.HiddenAppsManager
import com.geecee.escapelauncher.utils.managers.ScreenTimeManager
import com.geecee.escapelauncher.utils.managers.getUsageForApp
import com.geecee.escapelauncher.utils.managers.scheduleDailyCleanup
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext // The app context

    fun getContext(): Context = appContext // Returns the context

    var appTheme: MutableState<ColorScheme> = mutableStateOf(offLightScheme)

    // Managers

    val favoriteAppsManager: FavoriteAppsManager =
        FavoriteAppsManager(application) // Favorite apps manager

    val hiddenAppsManager: HiddenAppsManager = HiddenAppsManager(application) // Hidden apps manager

    val challengesManager: ChallengesManager =
        ChallengesManager(application) // Manager for challenges

    // Other stuff

    var isAppOpened: Boolean =
        false // Set to true when an app is opened and false when it is closed again

    val isPrivateSpaceUnlocked: MutableState<Boolean> =
        mutableStateOf(false) // If the private space is unlocked, set by a registered receiver when the private space is closed or opened

    val shouldGoHomeOnResume: MutableState<Boolean> =
        mutableStateOf(false) // This is to check whether to go back to the first page of the home screen the next time onResume is called, It is only ever used once in AllApps when you come back from signing into private space

    // Screen time related things

    val today: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val screenTimeCache = mutableStateMapOf<String, Long>() // Cache mapping package name to screen time

    val shouldReloadScreenTime: MutableState<Int> =
        mutableIntStateOf(0) // This exists because the screen time is retrieved in LaunchedEffects so it'll reload when the value of this is changed

    fun updateAppScreenTime(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val screenTime = getUsageForApp(packageName, today)
            screenTimeCache[packageName] = screenTime
        }
    } // Function to update a single app's cached screen time

    @Suppress("unused")
    fun reloadScreenTimeCache(packageNames: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            packageNames.forEach { packageName ->
                val screenTime = getUsageForApp(packageName, today)
                screenTimeCache[packageName] = screenTime
            }
            shouldReloadScreenTime.value++
        }
    } // Reloads the screen times

    suspend fun getScreenTimeAsync(packageName: String, forceRefresh: Boolean = false): Long {
        if (forceRefresh || !screenTimeCache.containsKey(packageName)) {
            val screenTime = getUsageForApp(packageName, today)
            screenTimeCache[packageName] = screenTime
            return screenTime
        }
        return screenTimeCache[packageName] ?: 0L
    } // Function to get screen time from cache or compute if missing

    fun getCachedScreenTime(packageName: String): Long {
        return screenTimeCache[packageName] ?: 0L
    } // Non-suspend function that just returns the cached value without fetching
}

class MainHomeScreen : ComponentActivity() {
    private lateinit var privateSpaceReceiver: PrivateSpaceStateReceiver
    private lateinit var screenOffReceiver: ScreenOffReceiver
    private val homeScreenModel by viewModels<HomeScreenModel> {
        HomeScreenModelFactory(application, viewModel)
    }
    private val viewModel: MainAppViewModel by viewModels()

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    /**
     * Main Entry point
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup analytics
        configureAnalytics(
            getBooleanSetting(
                this,
                this.resources.getString(R.string.Analytics),
                false
            )
        )

        // Setup Splashscreen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        animateSplashScreen(splashScreen)

        // Make full screen
        enableEdgeToEdge()
        configureFullScreenMode()

        // Set up the screen time tracking
        ScreenTimeManager.initialize(this)
        scheduleDailyCleanup(this)
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            homeScreenModel.installedApps.forEach { app ->
                val screenTime = getUsageForApp(app.packageName, viewModel.today)
                viewModel.screenTimeCache[app.packageName] = screenTime
            }
            viewModel.shouldReloadScreenTime.value++
        }

        // Set up the application content
        setContent { SetUpContent() }

        // Register screen off receiver
        screenOffReceiver = ScreenOffReceiver {
            // Screen turned off
            if (viewModel.isAppOpened) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val packageName = homeScreenModel.currentSelectedApp.value.packageName
                    ScreenTimeManager.onAppClosed(packageName)

                    // Update screen time for just this app in the cache
                    viewModel.updateAppScreenTime(packageName)

                    // Trigger UI refresh
                    viewModel.shouldReloadScreenTime.value++

                    Log.i("INFO", "Screen turned off with app " + homeScreenModel.currentSelectedApp.value.packageName + " open, stopping screen time counting at " + AppUtils.formatScreenTime(viewModel.getCachedScreenTime(homeScreenModel.currentSelectedApp.value.packageName)))

                    // Reset state
                    homeScreenModel.currentSelectedApp =
                        mutableStateOf(InstalledApp("", "", ComponentName("", "")))
  }
                viewModel.isAppOpened = false
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)

        //Private space receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            privateSpaceReceiver = PrivateSpaceStateReceiver { isUnlocked ->
                viewModel.isPrivateSpaceUnlocked.value = isUnlocked
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
            registerReceiver(privateSpaceReceiver, intentFilter)
        }

        // Subscribe to notifications this is done in a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            Firebase.messaging.subscribeToTopic("updates")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("INFO", "Subscribed to FCM topic: updates")
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if we need to update screen time when coming back from an app
        if (viewModel.isAppOpened) {
            lifecycleScope.launch(Dispatchers.IO) {
                val packageName = homeScreenModel.currentSelectedApp.value.packageName
                ScreenTimeManager.onAppClosed(packageName)

                // Update screen time for just this app in the cache
                viewModel.updateAppScreenTime(packageName)

                // Trigger UI refresh
                viewModel.shouldReloadScreenTime.value++

                // Reset state
                homeScreenModel.currentSelectedApp =
                    mutableStateOf(InstalledApp("", "", ComponentName("", "")))
            }
            viewModel.isAppOpened = false
        }

        // Reset home
        try {
            AppUtils.resetHome(homeScreenModel, viewModel.shouldGoHomeOnResume.value)
            viewModel.shouldGoHomeOnResume.value = false
        } catch (ex: Exception) {
            Log.e("ERROR", ex.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the receivers
        if (::privateSpaceReceiver.isInitialized) {
            unregisterReceiver(privateSpaceReceiver)
        }
        if (::screenOffReceiver.isInitialized) {
            unregisterReceiver(screenOffReceiver)
        }
    }

    /**
     * Puts the app into full screen
     */
    @Suppress("DEPRECATION")
    private fun configureFullScreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    /**
     * The main parent composable for the app
     * Contains EscapeTheme & SetupNavHost
     */
    @Composable
    private fun SetUpContent() {
        val colorScheme: ColorScheme
        var settingToChange = stringResource(R.string.Theme)

        if (getBooleanSetting(this@MainHomeScreen, stringResource(R.string.autoThemeSwitch), false)) {
            settingToChange = if(isSystemInDarkTheme()){
                stringResource(R.string.dTheme)
            } else {
                stringResource(R.string.lTheme)
            }
        }

        // Set theme
        colorScheme = AppTheme.fromId(getIntSetting(this@MainHomeScreen,settingToChange, 11)).scheme

        // Set theme
        viewModel.appTheme = remember {
            mutableStateOf(colorScheme)
        }

        EscapeTheme(viewModel.appTheme) {
            SetupNavHost(determineStartDestination(LocalContext.current))
        }
    }

    /**
     * Determines the start location for the NavHost
     *
     * @param context The context of the app
     *
     * @author George Clensy
     *
     * @see Settings
     *
     * @return Returns "home" if it is not the first time and "onboarding" if it is
     */
    private fun determineStartDestination(context: Context): String {
        return when {
            getBooleanSetting(
                context,
                context.resources.getString(R.string.FirstTime),
                true
            ) -> "onboarding"

            else -> "home"
        }
    }

    /**
     * Sets up main navigation host for the app
     *
     * @param startDestination Where to start
     */
    @Composable
    private fun SetupNavHost(startDestination: String) {
        val navController = rememberNavController()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController, startDestination = startDestination) {
                composable(
                    "home",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    HomeScreenPageManager(
                        viewModel,
                        homeScreenModel
                    ) { navController.navigate("settings") }
                }
                composable(
                    "settings",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Settings(
                        viewModel,
                        { navController.navigate("home") },
                        this@MainHomeScreen,
                    )
                }
                composable(
                    "onboarding",
                    enterTransition = { fadeIn(tween(900)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Onboarding(
                        navController,
                        viewModel,
                        pushNotificationPermissionLauncher,
                        homeScreenModel,
                        this@MainHomeScreen
                    )
                }
            }
        }
    }
}