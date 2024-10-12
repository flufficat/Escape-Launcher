package com.geecee.escape.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.SwipeableState
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.geecee.escape.AppUtils
import com.geecee.escape.AppUtils.getCurrentTime
import com.geecee.escape.ChallengesManager
import com.geecee.escape.FavoriteAppsManager
import com.geecee.escape.HiddenAppsManager
import com.geecee.escape.OpenChallenge
import com.geecee.escape.R
import com.geecee.escape.WidgetsScreen
import com.geecee.escape.getWidgetHeight
import com.geecee.escape.getWidgetOffset
import com.geecee.escape.getWidgetWidth
import com.geecee.escape.ui.theme.JostTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(
    ExperimentalWearMaterialApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SwipeableHome(
    context: Context,
    packageManager: PackageManager,
    hiddenAppsManager: HiddenAppsManager,
    favoriteAppsManager: FavoriteAppsManager,
    challengesManager: ChallengesManager,
    onOpenSettings: () -> Unit
) {
    // Swipeable stuff
    val width = 1000.dp
    val squareSize = 1000.dp
    val swipeableState = rememberSwipeableState(1)
    val sizePx = with(LocalDensity.current) { squareSize.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1) // Maps anchor points (in px) to states

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
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(),
        Context.MODE_PRIVATE
    )
    val favoriteApps =
        remember { mutableStateListOf<String>().apply { addAll(favoriteAppsManager.getFavoriteApps()) } }
    val interactionSource = remember { MutableInteractionSource() }

    //Challenge stuff
    val showOpenChallenge = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(width)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(01f) },
                orientation = Orientation.Horizontal,
                velocityThreshold = 100.dp,
                resistance = null
            )
            .combinedClickable(onClick = {}, onLongClickLabel = {}.toString(), onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenSettings()

                val editor = sharedPreferencesSettings.edit()
                editor.putString("FirstTimeAppDrawHelp", "False")
                editor.apply()
            },
                indication = null, interactionSource = interactionSource
            )

    ) {

        HomeScreen(
            context = context,
            packageManager = packageManager,
            currentAppName = currentSelectedApp,
            currentPackageName = currentPackageName,
            isCurrentAppFavorite = isCurrentAppFavorite,
            isCurrentAppHidden = isCurrentAppHidden,
            isCurrentAppChallenged = isCurrentAppChallenge,
            showBottomSheet = showBottomSheet,
            favoriteAppsManager = favoriteAppsManager,
            hiddenAppsManager = hiddenAppsManager,
            challengesManager = challengesManager,
            showOpenChallenge = showOpenChallenge,
            sharedPreferencesSettings = sharedPreferencesSettings,
            favouriteApps = favoriteApps,
            swipeableState = swipeableState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
        )


        AppsList(
            context = context,
            packageManager = packageManager,
            swipeableState = swipeableState,
            currentAppName = currentSelectedApp,
            currentPackageName = currentPackageName,
            isCurrentAppFavorite = isCurrentAppFavorite,
            isCurrentAppHidden = isCurrentAppHidden,
            isCurrentAppChallenged = isCurrentAppChallenge,
            showBottomSheet = showBottomSheet,
            favoriteAppsManager = favoriteAppsManager,
            hiddenAppsManager = hiddenAppsManager,
            challengesManager = challengesManager,
            showOpenChallenge = showOpenChallenge,
            sharedPreferencesSettings = sharedPreferencesSettings
        )
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
                                context.startActivity(intent)
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!isCurrentAppHidden.value) {
                        Text(
                            stringResource(id = R.string.hide),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    hiddenAppsManager.addHiddenApp(currentPackageName.value)
                                    showBottomSheet.value = false
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
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
                                    favoriteAppsManager.removeFavoriteApp(currentPackageName.value)
                                    isCurrentAppFavorite.value = false
                                } else {
                                    favoriteAppsManager.addFavoriteApp(currentPackageName.value)
                                    isCurrentAppFavorite.value = true
                                }
                                updateFavorites(favoriteAppsManager, favoriteApps)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!isCurrentAppChallenge.value) {
                        Text(
                            stringResource(id = R.string.add_open_challenge),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    challengesManager.addChallengeApp(currentPackageName.value)
                                    showBottomSheet.value = false
                                    isCurrentAppChallenge.value = true
                                }),
                            MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
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
                                context.startActivity(intent)
                                showBottomSheet.value = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
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
                packageManager,
                context,
                currentPackageName.value,
                challengesManager,
                true,
                null
            )
            showOpenChallenge.value = false
        }, {
            showOpenChallenge.value = false
        })
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun HomeScreen(
    context: Context,
    packageManager: PackageManager,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showBottomSheet: MutableState<Boolean>,
    hiddenAppsManager: HiddenAppsManager,
    favoriteAppsManager: FavoriteAppsManager,
    challengesManager: ChallengesManager,
    showOpenChallenge: MutableState<Boolean>,
    sharedPreferencesSettings: SharedPreferences,
    favouriteApps: SnapshotStateList<String>,
    swipeableState: SwipeableState<Int>,
    modifier: Modifier
) {
    val scrollState = rememberLazyListState()


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
                Clock(sharedPreferencesSettings)
            }
        }

        item {
            var widgetOffset by remember { mutableIntStateOf(0) }
            widgetOffset =
                if (sharedPreferencesSettings.getString("HomeAlignment", "Center") == "Left") {
                    -8
                } else if (sharedPreferencesSettings.getString(
                        "HomeAlignment",
                        "Center"
                    ) == "Right"
                ) {
                    8
                } else {
                    0
                }
            widgetOffset += getWidgetOffset(context).toInt()

            if (sharedPreferencesSettings.getString("WidgetsToggle", "False") == "True") {
                WidgetsScreen(
                    context = context,
                    modifier = Modifier
                        .offset((widgetOffset).dp, 0.dp)
                        .size((getWidgetWidth(context)).dp, (getWidgetHeight(context)).dp)
                        .padding(0.dp, 7.dp)
                )
            }
        }

        items(favouriteApps) { app ->
            AppsListItem(
                AppUtils.getResolveInfoFromPackageName(app, packageManager),
                packageManager,
                context,
                showBottomSheet,
                currentAppName,
                currentPackageName,
                isCurrentAppFavorite,
                isCurrentAppHidden,
                isCurrentAppChallenged,
                showOpenChallenge,
                challengesManager,
                favoriteAppsManager,
                hiddenAppsManager,
                swipeableState,
                null
            )
        }
    }

}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun AppsList(
    context: Context,
    packageManager: PackageManager,
    swipeableState: SwipeableState<Int>,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showBottomSheet: MutableState<Boolean>,
    hiddenAppsManager: HiddenAppsManager,
    favoriteAppsManager: FavoriteAppsManager,
    challengesManager: ChallengesManager,
    showOpenChallenge: MutableState<Boolean>,
    sharedPreferencesSettings: SharedPreferences
) {
    val installedApps = AppUtils.getAllInstalledApps(packageManager = packageManager)
    val sortedInstalledApps =
        installedApps.sortedBy {
            AppUtils.getAppNameFromPackageName(
                context,
                it.activityInfo.packageName
            )
        }
    val scrollState = rememberLazyListState()
    var searchBoxText by remember { mutableStateOf("") }

    Box(
        Modifier
            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
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
                if (sharedPreferencesSettings.getString("showSearchBox", "True") == "True") {
                    Spacer(modifier = Modifier.height(15.dp))
                    AnimatedPillSearchBar({ searchText ->
                        searchBoxText = searchText
                        var autoOpen = false

                        if (sharedPreferencesSettings.getString(
                                "searchAutoOpen",
                                "False"
                            ) == "True"
                        ) {
                            autoOpen = true
                        }

                        if (autoOpen) {
                            if (AppUtils.filterAndSortApps(
                                    installedApps,
                                    searchText,
                                    packageManager
                                ).size == 1
                            ) {
                                val appInfo = AppUtils.filterAndSortApps(
                                    installedApps,
                                    searchText,
                                    packageManager
                                ).first()
                                currentPackageName.value =
                                    appInfo.activityInfo.packageName

                                AppUtils.openApp(
                                    packageManager,
                                    context,
                                    currentPackageName.value,
                                    challengesManager,
                                    false,
                                    openChallengeShow = showOpenChallenge
                                )

                            }
                        }
                    },
                        { searchText ->
                            if (AppUtils.filterAndSortApps(
                                    installedApps,
                                    searchText,
                                    packageManager
                                ).isNotEmpty()
                            ) {
                                val firstAppInfo = AppUtils.filterAndSortApps(
                                    installedApps,
                                    searchText,
                                    packageManager
                                ).first()
                                currentPackageName.value = firstAppInfo.activityInfo.packageName


                                AppUtils.openApp(
                                    packageManager,
                                    context,
                                    currentPackageName.value,
                                    challengesManager,
                                    false,
                                    null
                                )
                            }
                        })
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            items(sortedInstalledApps.filter { appInfo ->
                val appName = appInfo.loadLabel(packageManager).toString()
                appName.contains(searchBoxText, ignoreCase = true)
            }) { app ->
                if (app.activityInfo.packageName != "com.geecee.escape" && !hiddenAppsManager.isAppHidden(
                        app.activityInfo.packageName
                    )
                )
                    AppsListItem(
                        app,
                        context = context,
                        packageManager = packageManager,
                        currentAppName = currentAppName,
                        currentPackageName = currentPackageName,
                        isCurrentAppHidden = isCurrentAppHidden,
                        isCurrentAppChallenged = isCurrentAppChallenged,
                        isCurrentAppFavorite = isCurrentAppFavorite,
                        showBottomSheet = showBottomSheet,
                        challengesManager = challengesManager,
                        hiddenAppsManager = hiddenAppsManager,
                        favoriteAppsManager = favoriteAppsManager,
                        showOpenChallenge = showOpenChallenge,
                        swipeableState = swipeableState,
                        lazyListState = scrollState
                    )
            }


            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalWearMaterialApi::class)
@Composable
fun AppsListItem(
    app: ResolveInfo?,
    packageManager: PackageManager,
    context: Context,
    showBottomSheet: MutableState<Boolean>,
    currentAppName: MutableState<String>,
    currentPackageName: MutableState<String>,
    isCurrentAppFavorite: MutableState<Boolean>,
    isCurrentAppHidden: MutableState<Boolean>,
    isCurrentAppChallenged: MutableState<Boolean>,
    showOpenChallenge: MutableState<Boolean>,
    challengesManager: ChallengesManager,
    favoriteAppsManager: FavoriteAppsManager,
    hiddenAppsManager: HiddenAppsManager,
    swipeableState: SwipeableState<Int>,
    lazyListState: LazyListState?
) {
    val coroutineScope = rememberCoroutineScope()

    if (app != null) {

        Text(
            AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName),
            modifier = Modifier
                .padding(vertical = 15.dp)
                .combinedClickable(
                    onClick = {
                        val packageName = app.activityInfo.packageName
                        currentPackageName.value = packageName

                        AppUtils.openApp(
                            packageManager = packageManager,
                            context = context,
                            packageName,
                            challengesManager,
                            false,
                            showOpenChallenge
                        )

                        coroutineScope.launch {
                            delay(200)
                            swipeableState.animateTo(1, tween(1))

                            lazyListState?.scrollToItem(0)
                        }
                    },
                    onLongClick = {
                        showBottomSheet.value = true
                        currentAppName.value =
                            AppUtils.getAppNameFromPackageName(
                                context,
                                app.activityInfo.packageName
                            )
                        currentPackageName.value = app.activityInfo.packageName
                        isCurrentAppChallenged.value = challengesManager.doesAppHaveChallenge(

                            app.activityInfo.packageName

                        )
                        isCurrentAppHidden.value = hiddenAppsManager.isAppHidden(

                            app.activityInfo.packageName

                        )
                        isCurrentAppFavorite.value = favoriteAppsManager.isAppFavorite(
                            app.activityInfo.packageName
                        )

                    }
                ),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun AnimatedPillSearchBar(
    textChange: (searchText: String) -> Unit,
    keyboardDone: (searchText: String) -> Unit
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }

    // Animate the width of the search bar
    val width by animateDpAsState(targetValue = if (expanded) 280.dp else 150.dp, label = "")

    // Animate the alpha of the text field content
    val alpha by animateFloatAsState(targetValue = if (expanded) 1f else 0f, label = "")

    // FocusRequester to request focus on the text field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .width(width)
            .height(56.dp)
            .clickable {
                expanded = !expanded
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

            if (!expanded) {
                Text(
                    stringResource(id = R.string.search),
                    modifier = Modifier.animateContentSize(),
                    color = MaterialTheme.colorScheme.background,
                    style = JostTypography.bodyMedium
                )
            }

            if (expanded) {
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
fun Clock(sharedPreferencesSettings: SharedPreferences) {
    var time by remember { mutableStateOf(getCurrentTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTime()
            delay(1000) // Update every second
        }
    }

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


@Preview
@Composable
fun PreviewSearchBar() {
    AnimatedPillSearchBar({}, {})
}

fun updateFavorites(
    favoriteAppsManager: FavoriteAppsManager,
    favoriteApps: SnapshotStateList<String>
) {
    favoriteApps.clear()
    favoriteApps.addAll(favoriteAppsManager.getFavoriteApps())
}