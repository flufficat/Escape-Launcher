package com.geecee.escape

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.EscapeTheme
import com.geecee.escape.ui.views.HomeScreenModel
import com.geecee.escape.ui.views.HomeScreenPageManager
import com.geecee.escape.ui.views.Onboarding
import com.geecee.escape.ui.views.Settings
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.PrivateSpaceStateReceiver
import com.geecee.escape.utils.ScreenOffReceiver
import com.geecee.escape.utils.animateSplashScreen
import com.geecee.escape.utils.configureAnalytics
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.managers.ChallengesManager
import com.geecee.escape.utils.managers.FavoriteAppsManager
import com.geecee.escape.utils.managers.HiddenAppsManager
import com.geecee.escape.utils.managers.ScreenTimeManager
import com.geecee.escape.utils.managers.scheduleDailyCleanup
import com.geecee.escape.utils.managers.sendNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext
    val packageManager: PackageManager = application.packageManager
    val favoriteAppsManager: FavoriteAppsManager = FavoriteAppsManager(application)
    val hiddenAppsManager: HiddenAppsManager = HiddenAppsManager(application)
    val challengesManager: ChallengesManager = ChallengesManager(application)
    var isAppOpened: Boolean = false
    var currentPackageName: String? = null
    val showPrivateSpaceUnlockedUI: MutableState<Boolean> = mutableStateOf(false)

    // These are for getting the launchedEffect with the screen time tracking to reload so the screen time updates
    val shouldReloadAppUsage: MutableState<Boolean> = mutableStateOf(false)
    val shouldReloadAppUsageOnHome: MutableState<Boolean> = mutableStateOf(false)
    val shouldReloadAppUsageOnApps: MutableState<Boolean> = mutableStateOf(false)
    val shouldReloadTotalScreenTimeOnHomeScreen: MutableState<Boolean> = mutableStateOf(false)


    val shouldGoHomeOnResume: MutableState<Boolean> = mutableStateOf(true)

    fun getContext(): Context = appContext
}

class MainHomeScreen : ComponentActivity() {
    private lateinit var privateSpaceReceiver: PrivateSpaceStateReceiver
    private lateinit var screenOffReceiver: ScreenOffReceiver
    private lateinit var homeScreenModel: HomeScreenModel

    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

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

        // Set up the application content
        setContent { SetUpContent() }

        // Register screen off receiver
        screenOffReceiver = ScreenOffReceiver {
            // Screen turned off
            val viewModel: MainAppViewModel = ViewModelProvider(this)[MainAppViewModel::class.java]
            if (viewModel.isAppOpened) {
                if (viewModel.currentPackageName != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        ScreenTimeManager.onAppClosed(viewModel.currentPackageName!!)
                        viewModel.currentPackageName = null
                    }
                }
                viewModel.isAppOpened = false
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)

        //Private space receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            privateSpaceReceiver = PrivateSpaceStateReceiver { isUnlocked ->
                val viewModel: MainAppViewModel =
                    ViewModelProvider(this)[MainAppViewModel::class.java]
                viewModel.showPrivateSpaceUnlockedUI.value = isUnlocked
            }
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PROFILE_AVAILABLE)
                addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
            }
            registerReceiver(privateSpaceReceiver, intentFilter)
        }

        // Notification Perms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/georgeclensy/escape"))
        sendNotification(this, "TEST", "TEST NOTIFICATION", "updates", "Updates", notificationIntent)
    }

    override fun onResume() {
        super.onResume()

        //Updates the screen time when you close an app
        try {
            val viewModel: MainAppViewModel = ViewModelProvider(this)[MainAppViewModel::class.java]
            if (viewModel.isAppOpened) {
                if (viewModel.currentPackageName != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        ScreenTimeManager.onAppClosed(viewModel.currentPackageName!!)
                        viewModel.currentPackageName = null
                    }
                }
                viewModel.isAppOpened = false
            }
        } catch (ex: Exception) {
            Log.e("ERROR", ex.toString())
        }

        // Reset home
        try {
            val viewModel: MainAppViewModel = ViewModelProvider(this)[MainAppViewModel::class.java]
            AppUtils.resetHome(homeScreenModel, viewModel, viewModel.shouldGoHomeOnResume.value)
            viewModel.shouldGoHomeOnResume.value = true
            viewModel.shouldReloadAppUsage.value = true
            viewModel.shouldReloadAppUsageOnHome.value = true
            viewModel.shouldReloadAppUsageOnApps.value = true
            viewModel.shouldReloadTotalScreenTimeOnHomeScreen.value = true
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

    // Makes it fullscreen
    @Suppress("DEPRECATION")
    private fun configureFullScreenMode() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } else {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    // Set content
    @Composable
    private fun SetUpContent() {
        EscapeTheme {
            SetupNavHost(determineStartDestination(LocalContext.current))
        }
    }

    // Finds which screen to start on
    private fun determineStartDestination(context: Context): String {
        return when {
            getBooleanSetting(context, "FirstTime", true) -> "onboarding"
            else -> "home"
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SetUpHomeScreenModel(mainAppModel: MainAppViewModel) {
        homeScreenModel = HomeScreenModel(
            showBottomSheet = remember { mutableStateOf(false) },
            sheetState = rememberModalBottomSheetState(),
            currentSelectedApp = remember { mutableStateOf("") },
            currentPackageName = remember { mutableStateOf("") },
            isCurrentAppFavorite = remember { mutableStateOf(false) },
            isCurrentAppChallenged = remember { mutableStateOf(false) },
            isCurrentAppHidden = remember { mutableStateOf(false) },
            haptics = LocalHapticFeedback.current,
            sharedPreferences = mainAppModel.getContext().getSharedPreferences(
                R.string.settings_pref_file_name.toString(),
                Context.MODE_PRIVATE
            ),
            favoriteApps = remember { mutableStateListOf<String>().apply { addAll(mainAppModel.favoriteAppsManager.getFavoriteApps()) } },
            interactionSource = remember { MutableInteractionSource() },
            showOpenChallenge = remember { mutableStateOf(false) },
            pagerState = rememberPagerState(1, 0f) { 3 },
            coroutineScope = rememberCoroutineScope(),
            installedApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager),
            sortedInstalledApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager)
                .sortedBy {
                    AppUtils.getAppNameFromPackageName(
                        mainAppModel.getContext(),
                        it.activityInfo.packageName
                    )
                },
            appsListScrollState = rememberLazyListState(),
            searchText = remember { mutableStateOf("") },
            searchExpanded = remember { mutableStateOf(false) },
            showPrivateSpaceSettings = remember { mutableStateOf(false) }
        )
    }

    // Navigation host
    @Composable
    private fun SetupNavHost(startDestination: String) {
        val navController = rememberNavController()
        val mainAppViewModel: MainAppViewModel by viewModels {
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        }

        SetUpHomeScreenModel(mainAppViewModel)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = androidx.compose.material3.MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController, startDestination = startDestination) {
                composable("home",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    HomeScreenPageManager(
                        mainAppViewModel,
                        homeScreenModel
                    ) { navController.navigate("settings") }
                }
                composable("settings",
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Settings(
                        mainAppViewModel,
                        { navController.navigate("home") },
                        this@MainHomeScreen,
                    )
                }
                composable("onboarding",
                    enterTransition = { fadeIn(tween(900)) },
                    exitTransition = { fadeOut(tween(300)) }) {
                    Onboarding(navController, mainAppViewModel)
                }
            }
        }
    }
}