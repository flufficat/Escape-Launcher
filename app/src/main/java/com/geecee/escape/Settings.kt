package com.geecee.escape

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.JosefinTypography
import com.geecee.escape.ui.theme.JostTypography
import com.geecee.escape.ui.theme.LoraTypography

@Composable
fun Settings(
    context: Context,
    goHome: () -> Unit,
    activity: Activity,
    packageManager: PackageManager,
    hiddenAppsManager: HiddenAppsManager,
    challengesManager: ChallengesManager
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(20.dp, 20.dp, 20.dp, 11.dp)
    ) {

        val navController = rememberNavController()

        NavHost(navController = navController, "mainSettingsPage") {
            composable("mainSettingsPage",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                MainSettingsPage({ goHome() }, navController, context, activity)
            }
            composable("widgetOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                WidgetOptions(context, { navController.popBackStack() }, { goHome() })
            }
            composable("alignmentOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                AlignmentOptions(context) { navController.popBackStack() }
            }
            composable("hiddenApps",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                HiddenApps(
                    hiddenAppsManager = hiddenAppsManager,
                    packageManager = packageManager,
                    context = context
                ) { navController.popBackStack() }
            }
            composable("openChallenges",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                OpenChallenges(
                    context,
                    challengesManager = challengesManager,
                    packageManager
                ) { navController.popBackStack() }
            }
            composable("chooseFont",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                ChooseFont(context, activity) { navController.popBackStack() }
            }
            composable("devOptions",
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }) {
                DevOptions(context = context) { navController.popBackStack() }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goHome: () -> Unit,
    navController: NavController,
    context: Context,
    activity: Activity
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = {
                    goHome()
                })
                .padding(0.dp, 120.dp, 0.dp, 0.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.settings),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleMedium,
            )
        }


        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.light_theme),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getLightTheme(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleLightTheme(checked, context, activity)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.search_box),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getSearchBox(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleSearchBox(checked, context)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.auto_open),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getAutoOpen(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleAutoOpen(context, checked)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.dynamic_colour),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getDynamicColour(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleDynamicColour(context, checked, activity)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.show_clock),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getClock(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleClock(context, checked)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    navController.navigate("widgetOptions")
                })
        ) {
            Text(
                stringResource(id = R.string.widget_options),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    navController.navigate("alignmentOptions")
                })
        ) {
            Text(
                stringResource(id = R.string.alignments),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    navController.navigate("hiddenApps")
                })
        ) {
            Text(
                stringResource(id = R.string.manage_hidden_apps),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    navController.navigate("openChallenges")
                })
        ) {
            Text(
                stringResource(id = R.string.manage_open_challenges),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
            )
        }


        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    navController.navigate("chooseFont")
                })
        ) {
            Text(
                stringResource(id = R.string.choose_font),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    changeLauncher(context)
                })
        ) {
            Text(
                stringResource(id = R.string.make_default_launcher),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize()
                    .rotate(-45f),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            stringResource(id = R.string.escape_launcher) + " " + stringResource(id = R.string.app_version),
            Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {}, onLongClick = {
                    navController.navigate("devOptions")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetOptions(context: Context, goBack: () -> Unit, goHome: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(0.dp, 120.dp, 0.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier.combinedClickable(onClick = {
                goBack()
            })
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.widget_options),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.enable_widget),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getWidgetEnabled(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleWidgets(context, checked)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .combinedClickable {
                    changeWidget(context) {
                        goHome()
                    }
                }) {
            Text(
                stringResource(id = R.string.select_widget),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "",
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Box(
            Modifier.fillMaxWidth()
        )
        {
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            Row {
                Text(
                    stringResource(id = R.string.offset),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                sliderPosition = getWidgetOffset(context)

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        setWidgetOffset(context, sliderPosition)
                    },
                    valueRange = -20f..20f,
                    steps = 40,
                    modifier = Modifier
                        .fillMaxWidth(0.85F)
                        .align(Alignment.CenterVertically)
                        .padding(20.dp, 0.dp, 20.dp, 0.dp)
                )
            }
            Icon(
                Icons.Rounded.Refresh,
                "",
                Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterEnd)
                    .combinedClickable {
                        sliderPosition = 0F
                        setWidgetOffset(context, sliderPosition)
                    }
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlignmentOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = {
                    goBack()
                })
                .padding(0.dp, 120.dp, 0.dp, 0.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.alignments),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall,
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.align(Alignment.CenterStart)) {
                Text(
                    stringResource(id = R.string.align_home),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                SegmentedButtonGroup(
                    listOf(
                        stringResource(id = R.string.left),
                        stringResource(id = R.string.center),
                        stringResource(id = R.string.right)
                    ), getHomeAlignment(context)
                ) { index ->
                    changeHomeAlignment(context, index)
                }
            }
        }

        Box(Modifier.fillMaxWidth()) {
            Column {
                Text(
                    stringResource(id = R.string.vertically_align_home),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                SegmentedButtonGroup(
                    listOf(
                        stringResource(id = R.string.top),
                        stringResource(id = R.string.center),
                        stringResource(
                            id = R.string.bottom
                        )
                    ), getHomeVAlignment(context)
                ) { index ->
                    changeHomeVAlignment(context, index)
                }
            }
        }

        Box(Modifier.fillMaxWidth()) {
            Column {
                Text(
                    stringResource(id = R.string.align_apps_list),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                SegmentedButtonGroup(
                    listOf(
                        stringResource(id = R.string.left),
                        stringResource(id = R.string.center),
                        stringResource(id = R.string.right)
                    ), selectedButtonIndex = getAppsAlignment(context)
                ) { index ->
                    changeAppsAlignment(context, index)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HiddenApps(
    hiddenAppsManager: HiddenAppsManager,
    context: Context,
    packageManager: PackageManager,
    goBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = {
                    goBack()
                })
                .padding(0.dp, 120.dp, 0.dp, 0.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.manage_hidden_apps),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall,
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        val haptics = LocalHapticFeedback.current
        val hiddenApps = remember { mutableStateOf(hiddenAppsManager.getHiddenApps()) }

        for (app in hiddenApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    getAppNameFromPackageName(context, app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent = packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = hiddenAppsManager.getHiddenApps()
                        }),
                    color = MaterialTheme.colorScheme.primary,
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
                            hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = hiddenAppsManager.getHiddenApps()
                        }),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpenChallenges(
    context: Context,
    challengesManager: ChallengesManager,
    packageManager: PackageManager,
    goBack: () -> Unit
) {
    val challengeApps = remember { mutableStateOf(challengesManager.getChallengeApps()) }
    val haptics = LocalHapticFeedback.current

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = {
                    goBack()
                })
                .padding(0.dp, 120.dp, 0.dp, 0.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.manage_open_challenges),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall,
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        for (app in challengeApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    getAppNameFromPackageName(context, app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent = packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = challengesManager.getChallengeApps()
                        }),
                    color = MaterialTheme.colorScheme.primary,
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
                            challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = challengesManager.getChallengeApps()
                        }),
                    tint = MaterialTheme.colorScheme.primary,
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
        Row(
            modifier = Modifier
                .combinedClickable(onClick = {
                    goBack()
                })
                .padding(0.dp, 120.dp, 0.dp, 0.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = stringResource(id = R.string.choose_font),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall,
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Text(
            "Jost",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    changeFont(context, activity, "jost")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = JostTypography.bodyMedium
        )
        Text(
            "Josefin",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    changeFont(context, activity, "josefin")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = JosefinTypography.bodyMedium
        )
        Text(
            "Lora",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    changeFont(context, activity, "lora")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = LoraTypography.bodyMedium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevOptions(context: Context, goBack: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(0.dp, 120.dp, 0.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier.combinedClickable(onClick = {
                goBack()
            })
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxSize()
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Dev Options",
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        Box(Modifier.fillMaxWidth()) {
            Text(
                "First time",
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getFirstTime(context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    resetFirstTime(context)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}