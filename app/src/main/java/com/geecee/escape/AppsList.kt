package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.ui.theme.JostTypography

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    packageManager: PackageManager,
    context: Context,
    onCloseAppDrawer: () -> Unit,
    favoriteAppsManager: FavoriteAppsManager,
    hiddenAppsManager: HiddenAppsManager,
    challengesManager: ChallengesManager
) {
    val haptics = LocalHapticFeedback.current
    val installedApps = packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.GET_ACTIVITIES
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentSelectedApp by remember { mutableStateOf("") }
    var currentPackageName by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(favoriteAppsManager.isAppFavorite(currentPackageName)) }
    var isChallenge by remember {
        mutableStateOf(
            challengesManager.doesAppHaveChallenge(
                currentPackageName
            )
        )
    }
    val sharedPreferencesSettings: SharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    var showDialog by remember { mutableStateOf(false) }
    var hasSearchedOpenApp by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(0.dp, 50.dp, 0.dp, 0.dp)
            .imePadding()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = if (sharedPreferencesSettings.getString(
                    "AppsAlignment", "Center"
                ) == "Center"
            ) Alignment.CenterHorizontally else if (sharedPreferencesSettings.getString(
                    "AppsAlignment", "Center"
                ) == "Left"
            ) Alignment.Start else Alignment.End,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(30.dp, 0.dp, 30.dp, 140.dp)
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = stringResource(id = R.string.all_apps),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            val autoOpen = sharedPreferencesSettings.getString("searchAutoOpen", "False")
            var searchBoxText by remember { mutableStateOf("") }

            if (sharedPreferencesSettings.getString(
                    "showSearchBox",
                    "False"
                ) == "True" && !hasSearchedOpenApp
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedPillSearchBar({ searchText ->
                    searchBoxText = searchText

                    if (autoOpen == "True") {
                        val filteredApps = installedApps.filter { appInfo ->
                            val appName = appInfo.loadLabel(packageManager).toString()
                            appName.contains(searchText, ignoreCase = true)
                        }.sortedBy { sortIT -> sortIT.loadLabel(packageManager).toString() }

                        if (filteredApps.size == 1) { // If there is exactly one app matching the search
                            hasSearchedOpenApp = true
                            val appInfo = filteredApps.first()
                            currentPackageName = appInfo.activityInfo.packageName

                            if (challengesManager.doesAppHaveChallenge(currentPackageName)) {
                                showDialog = true
                            } else {

                                onCloseAppDrawer()
                                val launchIntent =
                                    packageManager.getLaunchIntentForPackage(currentPackageName)
                                if (launchIntent != null) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    val options = ActivityOptions.makeBasic()
                                    context.startActivity(launchIntent, options.toBundle())
                                }
                                hasSearchedOpenApp = false
                            }
                        }
                    }
                },
                    { searchText ->
                        val filteredApps = installedApps.filter { appInfo ->
                            val appName = appInfo.loadLabel(packageManager).toString()
                            appName.contains(searchText, ignoreCase = true)
                        }.sortedBy { it.loadLabel(packageManager).toString() }

                        if (filteredApps.isNotEmpty()) {
                            val firstAppInfo = filteredApps.first()
                            val packageName = firstAppInfo.activityInfo.packageName
                            currentPackageName = packageName
                            if (challengesManager.doesAppHaveChallenge(currentPackageName)) {
                                showDialog = true
                            } else {
                                val launchIntent =
                                    packageManager.getLaunchIntentForPackage(packageName)
                                if (launchIntent != null) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    val options = ActivityOptions.makeBasic()
                                    context.startActivity(launchIntent, options.toBundle())
                                    onCloseAppDrawer()
                                }
                            }
                        }
                    })
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredApps = installedApps.filter { appInfo ->
                val appName = appInfo.loadLabel(packageManager).toString()
                appName.contains(searchBoxText, ignoreCase = true)
            }.sortedBy { it.loadLabel(packageManager).toString() }

            filteredApps.forEach { appInfo ->
                if (!hiddenAppsManager.isAppHidden(appInfo.activityInfo.packageName) && appInfo.activityInfo.packageName != "com.geecee.escape") {
                    Text(
                        appInfo.loadLabel(packageManager).toString(),
                        modifier = Modifier
                            .padding(0.dp, 15.dp)
                            .combinedClickable(onClick = {
                                val packageName = appInfo.activityInfo.packageName
                                currentPackageName = appInfo.activityInfo.packageName
                                if (challengesManager.doesAppHaveChallenge(packageName)) {
                                    showDialog = true
                                } else {
                                    // Launch the app without a challenge
                                    val launchIntent =
                                        packageManager.getLaunchIntentForPackage(appInfo.activityInfo.packageName)
                                    if (launchIntent != null) {
                                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        val options = ActivityOptions.makeBasic()
                                        context.startActivity(launchIntent, options.toBundle())
                                    }
                                    onCloseAppDrawer()
                                }
                            }, onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showBottomSheet = true
                                currentSelectedApp = appInfo
                                    .loadLabel(packageManager)
                                    .toString()
                                currentPackageName = appInfo.activityInfo.packageName
                                isFavorite = favoriteAppsManager.isAppFavorite(currentPackageName)
                                isChallenge =
                                    challengesManager.doesAppHaveChallenge(currentPackageName)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 50.dp),
        horizontalArrangement = if (sharedPreferencesSettings.getString(
                "AppsAlignment", "Center"
            ) == "Center"
        ) Arrangement.Center else if (sharedPreferencesSettings.getString(
                "AppsAlignment", "Center"
            ) == "Left"
        ) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Button(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onCloseAppDrawer()
        }) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                "Open app drawer",
                tint = MaterialTheme.colorScheme.background
            )
        }
    }


    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false; },
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
                        currentSelectedApp,
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
                                    Uri.parse("package:$currentPackageName")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(id = R.string.hide),
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                hiddenAppsManager.addHiddenApp(currentPackageName)
                                showBottomSheet = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (isFavorite) stringResource(id = R.string.rem_from_fav) else stringResource(
                            id = R.string.add_to_fav
                        ),
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                if (isFavorite) {
                                    favoriteAppsManager.removeFavoriteApp(currentPackageName)
                                } else {
                                    favoriteAppsManager.addFavoriteApp(currentPackageName)
                                }
                                // Update the state after the operation
                                isFavorite =
                                    favoriteAppsManager.isAppFavorite(currentPackageName)
                            }),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!isChallenge) {
                        Text(
                            stringResource(id = R.string.add_open_challenge),
                            Modifier
                                .padding(0.dp, 10.dp)
                                .combinedClickable(onClick = {
                                    challengesManager.addChallengeApp(currentPackageName)
                                    showBottomSheet = false
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
                                        data = Uri.parse("package:$currentPackageName")
                                    }
                                context.startActivity(intent)
                                showBottomSheet = false
                            }),
                        MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        OpenChallenge({
            val launchIntent =
                packageManager.getLaunchIntentForPackage(currentPackageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val options = ActivityOptions.makeBasic()
                context.startActivity(launchIntent, options.toBundle())
            }
            onCloseAppDrawer()
            hasSearchedOpenApp = false
        }, {
            hasSearchedOpenApp = false
            onCloseAppDrawer()
        })
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
            },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
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
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background,
                    style = JostTypography.bodyMedium
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        textChange(searchText.text)
                    },
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f)
                        .focusRequester(focusRequester),
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

@Preview
@Composable
fun PreviewSearchBar() {
    AnimatedPillSearchBar({}, {})
}