package com.geecee.escape.ui.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.ui.theme.JostTypography
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.AppUtils.getCurrentTime
import com.geecee.escape.utils.OpenChallenge
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.getUsageForApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SwipeableHome(
    mainAppModel: MainAppModel,
    onOpenSettings: () -> Unit
) {

    //BottomSheet
    val showBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val currentSelectedApp = remember { mutableStateOf("") }
    val currentPackageName = remember { mutableStateOf("") }
    val isCurrentAppFavorite = remember { mutableStateOf(false) }
    val isCurrentAppChallenge = remember { mutableStateOf(false) }
    val isCurrentAppHidden = remember { mutableStateOf(false) }

    //Other
    val haptics = LocalHapticFeedback.current
    val sharedPreferencesSettings: SharedPreferences = mainAppModel.context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val favoriteApps =
        remember { mutableStateListOf<String>().apply { addAll(mainAppModel.favoriteAppsManager.getFavoriteApps()) } }
    val interactionSource = remember { MutableInteractionSource() }

    //Challenge stuff
    val showOpenChallenge = remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(0, 0f) { 2 }
    HorizontalPager(
        state = pagerState,
        Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {}, onLongClickLabel = {}.toString(),
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSettings()

                    val editor = sharedPreferencesSettings.edit()
                    editor.putString("FirstTimeAppDrawHelp", "False")
                    editor.apply()
                },
                indication = null, interactionSource = interactionSource
            )

    ) { page ->
        when (page) {
            0 -> HomeScreen(
                mainAppModel = mainAppModel,
                currentAppName = currentSelectedApp,
                currentPackageName = currentPackageName,
                isCurrentAppFavorite = isCurrentAppFavorite,
                isCurrentAppHidden = isCurrentAppHidden,
                isCurrentAppChallenged = isCurrentAppChallenge,
                showBottomSheet = showBottomSheet,
                showOpenChallenge = showOpenChallenge,
                sharedPreferencesSettings = sharedPreferencesSettings,
                favouriteApps = favoriteApps,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp, 90.dp),
                pagerState = pagerState
            )

            1 -> AppsList(
                mainAppModel = mainAppModel,
                currentAppName = currentSelectedApp,
                currentPackageName = currentPackageName,
                isCurrentAppFavorite = isCurrentAppFavorite,
                isCurrentAppHidden = isCurrentAppHidden,
                isCurrentAppChallenged = isCurrentAppChallenge,
                showBottomSheet = showBottomSheet,
                showOpenChallenge = showOpenChallenge,
                sharedPreferencesSettings = sharedPreferencesSettings,
                pagerState = pagerState
            )
        }
    }


    //Bottom Sheet
    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false; },
            sheetState = sheetState
        ) {
            Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                Row {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "App Options",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        currentSelectedApp.value,
                        Modifier,
                        MaterialTheme.colorScheme.primary,
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
                                    Uri.parse("package:${currentPackageName.value}")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                mainAppModel.context.startActivity(intent)
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = JostTypography.bodyMedium
                    )
                    if (!isCurrentAppHidden.value) {
                        Text(
                            stringResource(id = R.string.hide),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    mainAppModel.hiddenAppsManager.addHiddenApp(currentPackageName.value)
                                    showBottomSheet.value = false
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = JostTypography.bodyMedium
                        )
                    }
                    Text(
                        text = if (isCurrentAppFavorite.value) stringResource(id = R.string.rem_from_fav) else stringResource(
                            id = R.string.add_to_fav
                        ),
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                if (isCurrentAppFavorite.value) {
                                    mainAppModel.favoriteAppsManager.removeFavoriteApp(
                                        currentPackageName.value
                                    )
                                    isCurrentAppFavorite.value = false
                                } else {
                                    mainAppModel.favoriteAppsManager.addFavoriteApp(
                                        currentPackageName.value
                                    )
                                    isCurrentAppFavorite.value = true
                                }
                                updateFavorites(mainAppModel, favoriteApps)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = JostTypography.bodyMedium
                    )
                    if (!isCurrentAppChallenge.value) {
                        Text(
                            stringResource(id = R.string.add_open_challenge),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    mainAppModel.challengesManager.addChallengeApp(
                                        currentPackageName.value
                                    )
                                    showBottomSheet.value = false
                                    isCurrentAppChallenge.value = true
                                }),
                            MaterialTheme.colorScheme.primary,
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
                                            Uri.parse("package:${currentPackageName.value}")
                                    }
                                mainAppModel.context.startActivity(intent)
                                showBottomSheet.value = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = JostTypography.bodyMedium
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showOpenChallenge.value,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge({
            AppUtils.openApp(
                currentPackageName.value,
                true,
                null,
                mainAppModel
            )
            showOpenChallenge.value = false
        }, {
            showOpenChallenge.value = false
        })
    }
}

@Composable
fun HomeScreen(
    mainAppModel: MainAppModel,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showBottomSheet: MutableState<Boolean>,
    showOpenChallenge: MutableState<Boolean>,
    sharedPreferencesSettings: SharedPreferences,
    favouriteApps: SnapshotStateList<String>,
    modifier: Modifier,
    pagerState: PagerState
) {
    val scrollState = rememberLazyListState()
    val noApps = remember { mutableStateOf(true) }

    LazyColumn(
        state = scrollState,
        verticalArrangement = if (sharedPreferencesSettings.getString(
                "HomeVAlignment", "Center"
            ) == "Center"
        ) Arrangement.Center else if (sharedPreferencesSettings.getString(
                "HomeVAlignment", "Center"
            ) == "Top"
        ) Arrangement.Top else Arrangement.Bottom,
        horizontalAlignment = if (sharedPreferencesSettings.getString(
                "HomeAlignment", "Center"
            ) == "Center"
        ) Alignment.CenterHorizontally else if (sharedPreferencesSettings.getString(
                "HomeAlignment", "Center"
            ) == "Left"
        ) Alignment.Start else Alignment.End,
        modifier = modifier
    ) {
        item {
            if (sharedPreferencesSettings.getString("ShowClock", "True") == "True") {
                Clock(sharedPreferencesSettings, mainAppModel, noApps)
            }
        }

        items(favouriteApps) { app ->
            AppsListItem(
                AppUtils.getResolveInfoFromPackageName(app, mainAppModel.packageManager),
                mainAppModel = mainAppModel,
                showBottomSheet,
                currentAppName,
                currentPackageName,
                isCurrentAppFavorite,
                isCurrentAppHidden,
                isCurrentAppChallenged,
                showOpenChallenge,
                null,
                null,
                null,
                pagerState
            )
        }
    }

}

@Composable
fun AppsList(
    mainAppModel: MainAppModel,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showBottomSheet: MutableState<Boolean>,
    showOpenChallenge: MutableState<Boolean>,
    sharedPreferencesSettings: SharedPreferences,
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()
    val installedApps = AppUtils.getAllInstalledApps(packageManager = mainAppModel.packageManager)
    val sortedInstalledApps =
        installedApps.sortedBy {
            AppUtils.getAppNameFromPackageName(
                mainAppModel.context,
                it.activityInfo.packageName
            )
        }
    val scrollState = rememberLazyListState()
    val searchText = remember { mutableStateOf("") }
    val searchExpanded = remember { mutableStateOf(false) }

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp),
            horizontalAlignment = AppUtils.getAppsListAlignmentFromPreferences(
                sharedPreferencesSettings
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(140.dp))
                Text(
                    text = stringResource(id = R.string.all_apps),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                if (sharedPreferencesSettings.getBoolean("showSearchBox", true)) {
                    Spacer(modifier = Modifier.height(15.dp))
                    AnimatedPillSearchBar(
                        { searchBoxText ->
                            searchText.value = searchBoxText
                            var autoOpen = false

                            if (sharedPreferencesSettings.getBoolean(
                                    "searchAutoOpen",
                                    false
                                )
                            ) {
                                autoOpen = true
                            }

                            if (autoOpen) {
                                if (AppUtils.filterAndSortApps(
                                        installedApps,
                                        searchText.value,
                                        mainAppModel.packageManager
                                    ).size == 1
                                ) {
                                    val appInfo = AppUtils.filterAndSortApps(
                                        installedApps,
                                        searchText.value,
                                        mainAppModel.packageManager
                                    ).first()
                                    currentPackageName.value =
                                        appInfo.activityInfo.packageName

                                    AppUtils.openApp(
                                        currentPackageName.value,
                                        false,
                                        showOpenChallenge,
                                        mainAppModel
                                    )

                                    coroutineScope.launch {
                                        delay(200)
                                        pagerState.animateScrollToPage(0)
                                        scrollState.scrollToItem(0)
                                        searchExpanded.value = false
                                        searchText.value = ""
                                    }

                                }
                            }
                        },
                        { searchBoxText ->
                            if (AppUtils.filterAndSortApps(
                                    installedApps,
                                    searchBoxText,
                                    mainAppModel.packageManager
                                ).isNotEmpty()
                            ) {
                                val firstAppInfo = AppUtils.filterAndSortApps(
                                    installedApps,
                                    searchBoxText,
                                    mainAppModel.packageManager
                                ).first()

                                val packageName = firstAppInfo.activityInfo.packageName
                                currentPackageName.value = packageName

                                AppUtils.openApp(
                                    currentPackageName.value,
                                    false,
                                    showOpenChallenge,
                                    mainAppModel
                                )

                                coroutineScope.launch {
                                    delay(200)
                                    pagerState.animateScrollToPage(0)
                                    scrollState.scrollToItem(0)
                                    searchExpanded.value = false
                                    searchText.value = ""
                                }
                            }
                        },
                        searchExpanded
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            items(sortedInstalledApps.filter { appInfo ->
                val appName = appInfo.loadLabel(mainAppModel.packageManager).toString()
                appName.contains(searchText.value, ignoreCase = true)
            })
            { app ->
                if (app.activityInfo.packageName != "com.geecee.escape" && !mainAppModel.hiddenAppsManager.isAppHidden(
                        app.activityInfo.packageName
                    )
                )
                    AppsListItem(
                        app,
                        mainAppModel = mainAppModel,
                        currentAppName = currentAppName,
                        currentPackageName = currentPackageName,
                        isCurrentAppHidden = isCurrentAppHidden,
                        isCurrentAppChallenged = isCurrentAppChallenged,
                        isCurrentAppFavorite = isCurrentAppFavorite,
                        showBottomSheet = showBottomSheet,
                        showOpenChallenge = showOpenChallenge,
                        lazyListState = scrollState,
                        searchExpanded = searchExpanded,
                        searchText = searchText,
                        pagerState = pagerState
                    )
            }


            item {
                Spacer(modifier = Modifier.height(90.dp))
            }

            //TODO: Private space
//            item {
//                val isPrivateSpaceVisible by remember { mutableStateOf(false) }
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    Button(onClick = {
//                        if(isPrivateSpace(mainAppModel.context)) {
//                            // Hide private space
//                            lockPrivateSpace(mainAppModel.context)
//                        } else {
//                            // Show private space
//                            unlockPrivateSpace(mainAppModel.context)
//                        }
//                    }) {
//                        Text(if (isPrivateSpaceVisible) "Hide Private Space" else "Show Private Space")
//                    }
//                }
//            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppsListItem(
    app: ResolveInfo?,
    mainAppModel: MainAppModel,
    showBottomSheet: MutableState<Boolean>,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showOpenChallenge: MutableState<Boolean>,
    lazyListState: LazyListState?,
    searchExpanded: MutableState<Boolean>?,
    searchText: MutableState<String>?,
    pagerState: PagerState
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
                            currentPackageName.value = packageName

                            AppUtils.openApp(
                                packageName,
                                false,
                                showOpenChallenge,
                                mainAppModel
                            )

                            coroutineScope.launch {
                                delay(200)
                                pagerState.animateScrollToPage(0)
                                lazyListState?.scrollToItem(0)
                                searchExpanded?.value = false
                                searchText?.value = ""
                            }
                        },
                        onLongClick = {
                            showBottomSheet.value = true
                            currentAppName.value =
                                AppUtils.getAppNameFromPackageName(
                                    mainAppModel.context,
                                    app.activityInfo.packageName
                                )
                            currentPackageName.value = app.activityInfo.packageName
                            isCurrentAppChallenged.value =
                                mainAppModel.challengesManager.doesAppHaveChallenge(

                                    app.activityInfo.packageName

                                )
                            isCurrentAppHidden.value = mainAppModel.hiddenAppsManager.isAppHidden(

                                app.activityInfo.packageName

                            )
                            isCurrentAppFavorite.value =
                                mainAppModel.favoriteAppsManager.isAppFavorite(
                                    app.activityInfo.packageName
                                )

                        }
                    ),
                color = MaterialTheme.colorScheme.primary,
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
                                currentPackageName.value = packageName

                                AppUtils.openApp(
                                    packageName,
                                    false,
                                    showOpenChallenge,
                                    mainAppModel
                                )

                                coroutineScope.launch {
                                    delay(200)
                                    pagerState.animateScrollToPage(0)
                                    lazyListState?.scrollToItem(0)
                                    searchExpanded?.value = false
                                    searchText?.value = ""
                                }
                            },
                            onLongClick = {
                                showBottomSheet.value = true
                                currentAppName.value =
                                    AppUtils.getAppNameFromPackageName(
                                        mainAppModel.context,
                                        app.activityInfo.packageName
                                    )
                                currentPackageName.value = app.activityInfo.packageName
                                isCurrentAppChallenged.value =
                                    mainAppModel.challengesManager.doesAppHaveChallenge(

                                        app.activityInfo.packageName

                                    )
                                isCurrentAppHidden.value = mainAppModel.hiddenAppsManager.isAppHidden(

                                    app.activityInfo.packageName

                                )
                                isCurrentAppFavorite.value =
                                    mainAppModel.favoriteAppsManager.isAppFavorite(
                                        app.activityInfo.packageName
                                    )

                            }
                        ),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AnimatedPillSearchBar(
    textChange: (searchText: String) -> Unit,
    keyboardDone: (searchText: String) -> Unit,
    expanded: MutableState<Boolean>
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }


    // Animate the width of the search bar
    val width by animateDpAsState(targetValue = if (expanded.value) 280.dp else 150.dp, label = "")

    // Animate the alpha of the text field content
    val alpha by animateFloatAsState(targetValue = if (expanded.value) 1f else 0f, label = "")

    // FocusRequester to request focus on the text field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded.value) {
        if (expanded.value) {
            searchText = TextFieldValue("")
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .width(width)
            .height(56.dp)
            .clickable {
                expanded.value = !expanded.value
            }
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .animateContentSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .padding(5.dp, 0.dp)
                    .size(25.dp)
            )

            if (!expanded.value) {
                Text(
                    stringResource(id = R.string.search),
                    modifier = Modifier.animateContentSize(),
                    color = MaterialTheme.colorScheme.background,
                    style = JostTypography.bodyMedium
                )
            }

            if (expanded.value) {
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                        .animateContentSize()
                )

                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        textChange(searchText.text)
                    },
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .animateContentSize(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (alpha > 0) {
                            innerTextField()
                        }
                    },
                    maxLines = 1,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            keyboardDone(searchText.text)
                        }
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Clock(
    sharedPreferencesSettings: SharedPreferences,
    mainAppModel: MainAppModel,
    noApps: MutableState<Boolean>
) {
    var time by remember { mutableStateOf(getCurrentTime()) }
    val parts = time.split(":")
    val hours = parts[0]
    val minutes = parts[1]

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTime()
            delay(1000) // Update every second
        }
    }

    if (getBooleanSetting(context = mainAppModel.context,"BigClock",false)) {

        Column {
            Text(
                text = hours,
                modifier = Modifier.offset(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = if (!noApps.value) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )
            Text(
                text = minutes,
                color = MaterialTheme.colorScheme.primary,
                style = if (!noApps.value) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                modifier = Modifier.offset(0.dp)
            )
        }
    } else {
        Text(
            text = time,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = if (sharedPreferencesSettings.getString(
                    "HomeAlignment",
                    "Center"
                ) == "Left"
            ) Modifier.offset((0).dp) else if (sharedPreferencesSettings.getString(
                    "HomeAlignment",
                    "Center"
                ) == "Right"
            ) Modifier.offset(0.dp) else Modifier.offset(0.dp)
        )
    }
}

// Reloads favourite apps
fun updateFavorites(
    mainAppModel: MainAppModel,
    favoriteApps: SnapshotStateList<String>
) {
    favoriteApps.clear()
    favoriteApps.addAll(mainAppModel.favoriteAppsManager.getFavoriteApps())
}