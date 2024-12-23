package com.geecee.escape.ui.views

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.ui.theme.PitchDarkColorScheme
import com.geecee.escape.ui.theme.darkScheme
import com.geecee.escape.ui.theme.lightScheme
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.AppUtils.loadTextFromAssets
import com.geecee.escape.utils.changeAppsAlignment
import com.geecee.escape.utils.changeHomeAlignment
import com.geecee.escape.utils.changeHomeVAlignment
import com.geecee.escape.utils.changeLauncher
import com.geecee.escape.utils.changeTheme
import com.geecee.escape.utils.getAppsAlignment
import com.geecee.escape.utils.getBooleanSetting
import com.geecee.escape.utils.getHomeAlignment
import com.geecee.escape.utils.getHomeVAlignment
import com.geecee.escape.utils.resetActivity
import com.geecee.escape.utils.setBooleanSetting
import com.geecee.escape.utils.setStringSetting
import com.geecee.escape.utils.toggleBooleanSetting

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsHeader(goHome: () -> Unit, title: String) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = { goHome() })
            .padding(0.dp, 120.dp, 0.dp, 0.dp)
            .height(70.dp) // Set a fixed height for the header
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Go Back",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontSize = if (title.length > 11) 35.sp else MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(checked) }

    Box(Modifier.fillMaxWidth()) {
        Text(
            label,
            Modifier.padding(0.dp, 15.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            },
            Modifier.align(Alignment.CenterEnd)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsNavigationItem(
    label: String,
    diagonalArrow: Boolean?,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick)
    ) {
        Text(
            label,
            Modifier.padding(0.dp, 15.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        if (!diagonalArrow!!) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize()
                    .rotate(-45f),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
fun Settings(
    mainAppModel: MainAppModel,
    goHome: () -> Unit,
    activity: Activity,
) {
    val showPolicyDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp, 0.dp, 20.dp, 0.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable("mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage({ goHome() }, {showPolicyDialog.value = true}, navController, mainAppModel)
            }
            composable("alignmentOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                AlignmentOptions(mainAppModel.context) { navController.popBackStack() }
            }
            composable("hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable("openChallenges",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                OpenChallenges(
                    mainAppModel
                ) { navController.popBackStack() }
            }
            composable("chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(mainAppModel.context, activity) { navController.popBackStack() }
            }
            composable("devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions(context = mainAppModel.context) { navController.popBackStack() }
            }
            composable("theme",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ThemeOptions(mainAppModel.context, activity) { navController.popBackStack() }
            }
        }
    }

    if (showPolicyDialog.value) {
        PrivacyPolicyDialog(mainAppModel,showPolicyDialog)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goHome: () -> Unit,
    showPolicyDialog: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel
) {

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goHome, stringResource(R.string.settings))
        SettingsSwitch(
            label = stringResource(id = R.string.Analytics),
            checked = getBooleanSetting(
                mainAppModel.context,
                stringResource(R.string.Analytics),
                true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.context,
                    it,
                    mainAppModel.context.resources.getString(R.string.Analytics)
                )
            }
        )
        SettingsSwitch(
            label = stringResource(id = R.string.search_box),
            checked = getBooleanSetting(
                mainAppModel.context,
                stringResource(R.string.ShowSearchBox),
                true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.context,
                    it,
                    mainAppModel.context.resources.getString(R.string.ShowSearchBox)
                )
            }
        )
        SettingsSwitch(
            label = stringResource(id = R.string.auto_open),
            checked = getBooleanSetting(
                mainAppModel.context,
                stringResource(R.string.SearchAutoOpen)
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.context,
                    it,
                    mainAppModel.context.resources.getString(R.string.SearchAutoOpen)
                )
            }
        )
        SettingsSwitch(
            label = stringResource(id = R.string.show_clock),
            checked = getBooleanSetting(
                mainAppModel.context,
                stringResource(R.string.ShowClock),
                true
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.context,
                    it,
                    mainAppModel.context.resources.getString(R.string.ShowClock)
                )
            }
        )
        SettingsSwitch(
            label = stringResource(id = R.string.big_clock),
            checked = getBooleanSetting(mainAppModel.context, stringResource(R.string.BigClock)),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.context,
                    it,
                    mainAppModel.context.resources.getString(R.string.BigClock)
                )
            }
        )
        SettingsSwitch(
            label = stringResource(id = R.string.screen_time_on_app),
            checked = getBooleanSetting(
                mainAppModel.context,
                stringResource(R.string.ScreenTimeOnApp)
            ),
            onCheckedChange = {
                toggleBooleanSetting(
                    mainAppModel.context,
                    it,
                    mainAppModel.context.resources.getString(R.string.ScreenTimeOnApp)
                )
            }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.read_privacy_policy),
            false,
            onClick = { showPolicyDialog() }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.theme),
            false,
            onClick = { navController.navigate("theme") }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.alignments),
            false,
            onClick = { navController.navigate("alignmentOptions") }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.manage_hidden_apps),
            false,
            onClick = { navController.navigate("hiddenApps") }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.manage_open_challenges),
            false,
            onClick = { navController.navigate("openChallenges") }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.choose_font), false,
            onClick = { navController.navigate("chooseFont") }
        )
        SettingsNavigationItem(
            label = stringResource(id = R.string.make_default_launcher),
            true,
            onClick = { changeLauncher(mainAppModel.context) }
        )
        HorizontalDivider(Modifier.padding(0.dp, 15.dp))
        Text(
            stringResource(id = R.string.escape_launcher) + " " + stringResource(id = R.string.app_version),
            Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {}, onLongClick = {
                    navController.navigate("devOptions")
                }),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(25.dp))
    }
}

@Composable
fun AlignmentOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.alignments))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(id = R.string.home),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterStart),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                androidx.compose.runtime.mutableIntStateOf(
                    getHomeAlignment(context)
                )
            }
            val options = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterEnd)
                    .width(275.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            selectedIndex = index
                            changeHomeAlignment(context, selectedIndex)
                        },
                        selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            var selectedIndex by remember {
                androidx.compose.runtime.mutableIntStateOf(
                    getHomeVAlignment(context)
                )
            }
            val options = listOf(
                stringResource(R.string.top),
                stringResource(R.string.center),
                stringResource(R.string.bottom)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterEnd)
                    .width(275.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            selectedIndex = index
                            changeHomeVAlignment(context, selectedIndex)
                        },
                        selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .padding(0.dp, 15.dp)
        ) {
            Text(
                stringResource(id = R.string.apps),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterStart),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                androidx.compose.runtime.mutableIntStateOf(
                    getAppsAlignment(context)
                )
            }
            val options = listOf(
                stringResource(R.string.left),
                stringResource(R.string.center),
                stringResource(R.string.right)
            )
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .padding(0.dp, 0.dp)
                    .align(Alignment.CenterEnd)
                    .width(275.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            selectedIndex = index
                            changeAppsAlignment(context, selectedIndex)
                        },
                        selected = index == selectedIndex
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: Int, context: Context,
    activity: Activity
) {
    Box(
        Modifier
            .size(120.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable {
                changeTheme(theme, context, activity)
            }
    ) {
        Box(
            Modifier
                .padding(10.dp)
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (theme) {
                        0 ->
                            darkScheme.background

                        1 ->
                            lightScheme.background

                        2 ->
                            PitchDarkColorScheme.background

                        3 ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                dynamicDarkColorScheme(context).background
                            } else {
                                darkScheme.background
                            }

                        4 ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                dynamicLightColorScheme(context).background
                            } else {
                                lightScheme.background
                            }

                        else ->
                            darkScheme.background
                    }
                )
        ) {
            Text(
                when (theme) {
                    0 ->
                        stringResource(R.string.dark)

                    1 ->
                        stringResource(R.string.light)

                    2 ->
                        stringResource(R.string.pitch_black)

                    3 ->
                        stringResource(R.string.material_dark)

                    4 ->
                        stringResource(R.string.material_light)

                    else ->
                        stringResource(R.string.theme)
                },
                Modifier
                    .align(Alignment.Center)
                    .padding(5.dp),
                when (theme) {
                    0 ->
                        darkScheme.onBackground

                    1 ->
                        lightScheme.onBackground

                    2 ->
                        PitchDarkColorScheme.onBackground

                    3 ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            dynamicDarkColorScheme(context).onBackground
                        } else {
                            darkScheme.primary
                        }

                    4 ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            dynamicLightColorScheme(context).onBackground
                        } else {
                            lightScheme.onBackground
                        }

                    else ->
                        darkScheme.onBackground
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ThemeOptions(
    context: Context,
    activity: Activity,
    goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
    ) {
        SettingsHeader(goBack, stringResource(R.string.theme))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        LazyVerticalGrid(
            GridCells.Adaptive(minSize = 128.dp)
        ) {
            item {
                ThemeCard(0, context, activity)
            }
            item {
                ThemeCard(1, context, activity)
            }
            item {
                ThemeCard(2, context, activity)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    ThemeCard(3, context, activity)
                }
                item {
                    ThemeCard(4, context, activity)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenApps(
    mainAppModel: MainAppModel,
    goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.hidden_apps))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        val haptics = LocalHapticFeedback.current
        val hiddenApps = remember { mutableStateOf(mainAppModel.hiddenAppsManager.getHiddenApps()) }

        for (app in hiddenApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    AppUtils.getAppNameFromPackageName(mainAppModel.context, app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                mainAppModel.packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeBasic()
                                mainAppModel.context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )

                Icon(
                    Icons.Sharp.Close,
                    "",
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .fillMaxSize()
                        .combinedClickable(onClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpenChallenges(
    mainAppModel: MainAppModel,
    goBack: () -> Unit
) {
    val challengeApps =
        remember { mutableStateOf(mainAppModel.challengesManager.getChallengeApps()) }
    val haptics = LocalHapticFeedback.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.open_challenges))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        for (app in challengeApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    AppUtils.getAppNameFromPackageName(mainAppModel.context, app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                mainAppModel.packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeBasic()
                                mainAppModel.context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = mainAppModel.challengesManager.getChallengeApps()
                        }),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )

                Icon(
                    Icons.Sharp.Close,
                    "",
                    Modifier
                        .align(Alignment.CenterEnd)
                        .size(30.dp)
                        .fillMaxSize()
                        .combinedClickable(onClick = {
                            mainAppModel.challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = mainAppModel.challengesManager.getChallengeApps()
                        }),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseFont(context: Context, activity: Activity, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, stringResource(R.string.font))

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            "Jost",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Jost")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Inter",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Inter")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Lexend",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(context, context.resources.getString(R.string.Font), "Lexend")
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Work Sans",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    setStringSetting(
                        context,
                        context.resources.getString(R.string.Font),
                        "Work Sans"
                    )
                    resetActivity(context, activity)
                }),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DevOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader(goBack, "Developer Options")

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Box(Modifier.fillMaxWidth()) {
            Text(
                "First time",
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getBooleanSetting(context, stringResource(R.string.FirstTime), false)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    setBooleanSetting(
                        context,
                        context.resources.getString(R.string.FirstTime),
                        true
                    )
                    setBooleanSetting(
                        context,
                        context.resources.getString(R.string.FirstTimeAppDrawHelp),
                        true
                    )
                }, Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun PrivacyPolicyDialog(mainAppModel: MainAppModel, showPolicyDialog: MutableState<Boolean>){
    val scrollState = rememberScrollState()
    Column {
        Card(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)  // Make the content scrollable
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Load text from the asset
                loadTextFromAssets(mainAppModel.context, "Privacy Policy.txt")?.let { text ->
                    BasicText(
                        text = text, style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Normal
                        ), modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "OK" Button
                Button(
                    onClick = { showPolicyDialog.value = false },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                ) {
                    Text("OK")
                }
            }
        }
    }
}