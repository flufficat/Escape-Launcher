package com.geecee.escape.ui.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.ui.theme.JostTypography
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.OpenChallenge
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.getUsageForApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Model to be passed around home screen pages
data class HomeScreenModel @OptIn(ExperimentalMaterial3Api::class) constructor(
    //Bottom Sheet
    var showBottomSheet: MutableState<Boolean>,
    var sheetState: SheetState,
    var currentSelectedApp: MutableState<String>,
    var currentPackageName: MutableState<String>,
    var isCurrentAppFavorite: MutableState<Boolean>,
    var isCurrentAppChallenged: MutableState<Boolean>,
    var isCurrentAppHidden: MutableState<Boolean>,

    //Other
    var haptics: HapticFeedback,
    var sharedPreferences: SharedPreferences,
    var favoriteApps: SnapshotStateList<String>,
    var interactionSource: MutableInteractionSource,

    //Open Challenge
    var showOpenChallenge: MutableState<Boolean>,

    //Pages
    var pagerState: PagerState,

    //Apps Page
    var coroutineScope: CoroutineScope,
    val installedApps: MutableList<ResolveInfo>,
    var sortedInstalledApps: List<ResolveInfo>,
    val appsListScrollState: LazyListState,
    val searchText: MutableState<String>,
    val searchExpanded: MutableState<Boolean>
)

// Main composable for home screen - contains a pager with all the pages inside of it
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun HomeScreenPageManager(
    mainAppModel: MainAppModel,
    onOpenSettings: () -> Unit
) {

    //Set up variables
    val homeScreenModel = HomeScreenModel(
        //BottomSheet
        showBottomSheet = remember { mutableStateOf(false) },
        sheetState = rememberModalBottomSheetState(),
        currentSelectedApp = remember { mutableStateOf("") },
        currentPackageName = remember { mutableStateOf("") },
        isCurrentAppFavorite = remember { mutableStateOf(false) },
        isCurrentAppChallenged = remember { mutableStateOf(false) },
        isCurrentAppHidden = remember { mutableStateOf(false) },
        //Other
        haptics = LocalHapticFeedback.current,
        sharedPreferences = mainAppModel.context.getSharedPreferences(
            R.string.settings_pref_file_name.toString(),
            Context.MODE_PRIVATE
        ),
        favoriteApps = remember { mutableStateListOf<String>().apply { addAll(mainAppModel.favoriteAppsManager.getFavoriteApps()) } },
        interactionSource = remember { MutableInteractionSource() },
        //Challenge stuff
        showOpenChallenge = remember { mutableStateOf(false) },
        pagerState = rememberPagerState(1, 0f) { 3 },
        //Apps List
        coroutineScope = rememberCoroutineScope(),
        installedApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager),
        sortedInstalledApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager).sortedBy {
            AppUtils.getAppNameFromPackageName(
                mainAppModel.context,
                it.activityInfo.packageName
            )
        },
        appsListScrollState = rememberLazyListState(),
        searchText = remember { mutableStateOf("") },
        searchExpanded =  remember { mutableStateOf(false) }
    )

    // Home Screen Pages
    HorizontalPager(
        state = homeScreenModel.pagerState,
        Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {}, onLongClickLabel = {}.toString(),
                onLongClick = {
                    homeScreenModel.haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSettings()

                    val editor = homeScreenModel.sharedPreferences.edit()
                    editor.putString("FirstTimeAppDrawHelp", "False")
                    editor.apply()
                },
                indication = null, interactionSource = homeScreenModel.interactionSource
            )

    ) { page ->
        when (page) {
            0 -> ScreenTimeDashboard(mainAppModel)

            1 -> HomeScreen(
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp, 90.dp)
            )

            2 -> AppsList(
                mainAppModel = mainAppModel,
                homeScreenModel = homeScreenModel
            )
        }
    }


    //Bottom Sheet
    if (homeScreenModel.showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { homeScreenModel.showBottomSheet.value = false; },
            sheetState = homeScreenModel.sheetState
        ) {
            Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                Row {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "App Options",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        homeScreenModel.currentSelectedApp.value,
                        Modifier,
                        MaterialTheme.colorScheme.onSurface,
                        fontSize = 32.sp,
                        style = JostTypography.titleMedium
                    )
                }
                HorizontalDivider(Modifier.padding(0.dp, 15.dp))
                Column(Modifier.padding(47.dp, 0.dp, 0.dp, 0.dp)) {
                    Text(
                        stringResource(id = R.string.uninstall),
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                // Uninstall logic here
                                val intent = Intent(
                                    Intent.ACTION_DELETE,
                                    Uri.parse("package:${homeScreenModel.currentPackageName.value}")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                mainAppModel.context.startActivity(intent)
                            }),
                        MaterialTheme.colorScheme.onSurface,
                        style = JostTypography.bodyMedium
                    )
                    if (!homeScreenModel.isCurrentAppHidden.value) {
                        Text(
                            stringResource(id = R.string.hide),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    mainAppModel.hiddenAppsManager.addHiddenApp(homeScreenModel.currentPackageName.value)
                                    homeScreenModel.showBottomSheet.value = false
                                }),
                            MaterialTheme.colorScheme.onSurface,
                            style = JostTypography.bodyMedium
                        )
                    }
                    Text(
                        text = if (homeScreenModel.isCurrentAppFavorite.value) stringResource(id = R.string.rem_from_fav) else stringResource(
                            id = R.string.add_to_fav
                        ),
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                if (homeScreenModel.isCurrentAppFavorite.value) {
                                    mainAppModel.favoriteAppsManager.removeFavoriteApp(
                                        homeScreenModel.currentPackageName.value
                                    )
                                    homeScreenModel.isCurrentAppFavorite.value = false
                                } else {
                                    mainAppModel.favoriteAppsManager.addFavoriteApp(
                                        homeScreenModel.currentPackageName.value
                                    )
                                    homeScreenModel.isCurrentAppFavorite.value = true
                                }
                                updateFavorites(mainAppModel, homeScreenModel.favoriteApps)
                            }),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = JostTypography.bodyMedium
                    )
                    if (!homeScreenModel.isCurrentAppChallenged.value) {
                        Text(
                            stringResource(id = R.string.add_open_challenge),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    mainAppModel.challengesManager.addChallengeApp(
                                        homeScreenModel.currentPackageName.value
                                    )
                                    homeScreenModel.showBottomSheet.value = false
                                    homeScreenModel.isCurrentAppChallenged.value = true
                                }),
                            MaterialTheme.colorScheme.onSurface,
                            style = JostTypography.bodyMedium
                        )
                    }
                    Text(
                        stringResource(id = R.string.app_info),
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.parse("package:${homeScreenModel.currentPackageName.value}")
                                    }
                                mainAppModel.context.startActivity(intent)
                                homeScreenModel.showBottomSheet.value = false
                            }),
                        MaterialTheme.colorScheme.onSurface,
                        style = JostTypography.bodyMedium
                    )
                }
            }
        }
    }

    //Open Challenge
    AnimatedVisibility(
        visible = homeScreenModel.showOpenChallenge.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge({
            AppUtils.openApp(
                homeScreenModel.currentPackageName.value,
                true,
                null,
                mainAppModel
            )
            homeScreenModel.showOpenChallenge.value = false
        }, {
            homeScreenModel.showOpenChallenge.value = false
        })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsListItem(
    app: ResolveInfo?,
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel,
    lazyListState: LazyListState?,
    searchExpanded: MutableState<Boolean>?,
    searchText: MutableState<String>?
) {
    val coroutineScope = rememberCoroutineScope()

    if (app != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                AppUtils.getAppNameFromPackageName(
                    mainAppModel.context,
                    app.activityInfo.packageName
                ),
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .combinedClickable(
                        onClick = {
                            val packageName = app.activityInfo.packageName
                            homeScreenModel.currentPackageName.value = packageName

                            AppUtils.openApp(
                                packageName,
                                false,
                                homeScreenModel.showOpenChallenge,
                                mainAppModel
                            )

                            coroutineScope.launch {
                                delay(200)
                                homeScreenModel.pagerState.animateScrollToPage(1)
                                lazyListState?.scrollToItem(0)
                                searchExpanded?.value = false
                                searchText?.value = ""
                            }
                        },
                        onLongClick = {
                            homeScreenModel.showBottomSheet.value = true
                            homeScreenModel.currentSelectedApp.value =
                                AppUtils.getAppNameFromPackageName(
                                    mainAppModel.context,
                                    app.activityInfo.packageName
                                )
                            homeScreenModel.currentPackageName.value = app.activityInfo.packageName
                            homeScreenModel.isCurrentAppChallenged.value =
                                mainAppModel.challengesManager.doesAppHaveChallenge(
                                    app.activityInfo.packageName
                                )
                            homeScreenModel.isCurrentAppHidden.value =
                                mainAppModel.hiddenAppsManager.isAppHidden(
                                    app.activityInfo.packageName
                                )
                            homeScreenModel.isCurrentAppFavorite.value =
                                mainAppModel.favoriteAppsManager.isAppFavorite(
                                    app.activityInfo.packageName
                                )
                        }
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )

            if (getBooleanSetting(mainAppModel.context, "screenTimeOnApp")) {
                val appScreenTime = remember { androidx.compose.runtime.mutableLongStateOf(0L) }

                // Fetch screen time in a coroutine
                LaunchedEffect(app.activityInfo.packageName) {
                    withContext(Dispatchers.IO) {
                        appScreenTime.longValue = getUsageForApp(
                            app.activityInfo.packageName,
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        )
                    }
                }

                Text(
                    AppUtils.formatScreenTime(appScreenTime.longValue),
                    modifier = Modifier
                        .padding(vertical = 15.dp, horizontal = 5.dp)
                        .alpha(0.5f)
                        .combinedClickable(
                            onClick = {
                                val packageName = app.activityInfo.packageName
                                homeScreenModel.currentPackageName.value = packageName

                                AppUtils.openApp(
                                    packageName,
                                    false,
                                    homeScreenModel.showOpenChallenge,
                                    mainAppModel
                                )

                                coroutineScope.launch {
                                    delay(200)
                                    homeScreenModel.pagerState.animateScrollToPage(1)
                                    lazyListState?.scrollToItem(0)
                                    searchExpanded?.value = false
                                    searchText?.value = ""
                                }
                            },
                            onLongClick = {
                                homeScreenModel.showBottomSheet.value = true
                                homeScreenModel.currentSelectedApp.value =
                                    AppUtils.getAppNameFromPackageName(
                                        mainAppModel.context,
                                        app.activityInfo.packageName
                                    )
                                homeScreenModel.currentPackageName.value = app.activityInfo.packageName
                                homeScreenModel.isCurrentAppChallenged.value =
                                    mainAppModel.challengesManager.doesAppHaveChallenge(
                                        app.activityInfo.packageName
                                    )
                                homeScreenModel.isCurrentAppHidden.value =
                                    mainAppModel.hiddenAppsManager.isAppHidden(
                                        app.activityInfo.packageName
                                    )
                                homeScreenModel.isCurrentAppFavorite.value =
                                    mainAppModel.favoriteAppsManager.isAppFavorite(
                                        app.activityInfo.packageName
                                    )
                            }
                        ),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
