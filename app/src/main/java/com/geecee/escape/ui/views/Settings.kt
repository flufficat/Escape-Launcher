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
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.ui.theme.InterTypography
import com.geecee.escape.ui.theme.JostTypography
import com.geecee.escape.ui.theme.LexendTypography
import com.geecee.escape.ui.theme.WorkTypography
import com.geecee.escape.utils.AppUtils
import com.geecee.escape.utils.changeAppsAlignment
import com.geecee.escape.utils.changeFont
import com.geecee.escape.utils.changeHomeAlignment
import com.geecee.escape.utils.changeHomeVAlignment
import com.geecee.escape.utils.changeLauncher
import com.geecee.escape.utils.getAppsAlignment
import com.geecee.escape.utils.getAutoOpen
import com.geecee.escape.utils.getBigClock
import com.geecee.escape.utils.getClock
import com.geecee.escape.utils.getDynamicColour
import com.geecee.escape.utils.getFirstTime
import com.geecee.escape.utils.getHomeAlignment
import com.geecee.escape.utils.getHomeVAlignment
import com.geecee.escape.utils.getLightTheme
import com.geecee.escape.utils.getSearchBox
import com.geecee.escape.utils.resetFirstTime
import com.geecee.escape.utils.toggleAutoOpen
import com.geecee.escape.utils.toggleBigClock
import com.geecee.escape.utils.toggleClock
import com.geecee.escape.utils.toggleDynamicColour
import com.geecee.escape.utils.toggleLightTheme
import com.geecee.escape.utils.toggleSearchBox

@Composable
fun Settings(
    mainAppModel: MainAppModel,
    goHome: () -> Unit,
    activity: Activity,
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
                MainSettingsPage({ goHome() }, navController, mainAppModel, activity)
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainSettingsPage(
    goHome: () -> Unit,
    navController: NavController,
    mainAppModel: MainAppModel,
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
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getLightTheme(mainAppModel.context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleLightTheme(checked, mainAppModel.context, activity)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.search_box),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getSearchBox(mainAppModel.context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleSearchBox(checked, mainAppModel.context)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.auto_open),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getAutoOpen(mainAppModel.context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleAutoOpen(mainAppModel.context, checked)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(id = R.string.dynamic_colour),
                    Modifier.padding(0.dp, 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                var checked by remember { mutableStateOf(true) }
                checked = getDynamicColour(mainAppModel.context)

                Switch(
                    checked = checked, onCheckedChange = {
                        checked = it
                        toggleDynamicColour(mainAppModel.context, checked, activity)
                    }, Modifier.align(Alignment.CenterEnd)
                )
            }
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.show_clock),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getClock(mainAppModel.context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleClock(mainAppModel.context, checked)
                }, Modifier.align(Alignment.CenterEnd)
            )
        }

        Box(Modifier.fillMaxWidth()) {
            Text(
                stringResource(id = R.string.big_clock),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var checked by remember { mutableStateOf(true) }
            checked = getBigClock(mainAppModel.context)

            Switch(
                checked = checked, onCheckedChange = {
                    checked = it
                    toggleBigClock(mainAppModel.context, checked)
                }, Modifier.align(Alignment.CenterEnd)
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
                style = JostTypography.bodyMedium,
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
                style = JostTypography.bodyMedium,
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
                style = JostTypography.bodyMedium,
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
                style = JostTypography.bodyMedium,
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
                    changeLauncher(mainAppModel.context)
                })
        ) {
            Text(
                stringResource(id = R.string.make_default_launcher),
                Modifier.padding(0.dp, 15.dp),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
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
            style = JostTypography.bodyMedium,
            textAlign = TextAlign.Center,
        )
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

        Box(Modifier.fillMaxWidth().padding(0.dp, 15.dp)) {
            Text(
                stringResource(id = R.string.home),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterStart),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                androidx.compose.runtime.mutableIntStateOf(
                    getHomeAlignment(context)
                )
            }
            val options = listOf(stringResource(R.string.left), stringResource(R.string.center), stringResource(R.string.right))
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

        Box(Modifier.fillMaxWidth().padding(0.dp, 15.dp)) {
            var selectedIndex by remember {
                androidx.compose.runtime.mutableIntStateOf(
                    getHomeVAlignment(context)
                )
            }
            val options = listOf(stringResource(R.string.top), stringResource(R.string.center), stringResource(R.string.bottom))
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

        Box(Modifier.fillMaxWidth().padding(0.dp, 15.dp)) {
            Text(
                stringResource(id = R.string.apps),
                Modifier
                    .padding(0.dp, 5.dp)
                    .align(Alignment.CenterStart),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            var selectedIndex by remember {
                androidx.compose.runtime.mutableIntStateOf(
                    getAppsAlignment(context)
                )
            }
            val options = listOf(stringResource(R.string.left), stringResource(R.string.center), stringResource(R.string.right))
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
                text = stringResource(id = R.string.hidden_apps),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall,
            )
        }

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
                            val launchIntent = mainAppModel.packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    mainAppModel.context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                mainAppModel.context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.hiddenAppsManager.removeHiddenApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            hiddenApps.value = mainAppModel.hiddenAppsManager.getHiddenApps()
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
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
                    tint = MaterialTheme.colorScheme.primary,
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
    val challengeApps = remember { mutableStateOf(mainAppModel.challengesManager.getChallengeApps()) }
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
                text = stringResource(id = R.string.open_challenges),
                color = MaterialTheme.colorScheme.primary,
                style = JostTypography.titleSmall,
                fontSize = 40.sp
            )
        }

        HorizontalDivider(Modifier.padding(0.dp, 15.dp))

        for (app in challengeApps.value) {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    AppUtils.getAppNameFromPackageName(mainAppModel.context, app),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent = mainAppModel.packageManager.getLaunchIntentForPackage(app)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    mainAppModel.context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                mainAppModel.context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            mainAppModel.challengesManager.removeChallengeApp(app)
                            haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            challengeApps.value = mainAppModel.challengesManager.getChallengeApps()
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    style = JostTypography.bodyMedium
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
            "Inter",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    changeFont(context, activity, "inter")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = InterTypography.bodyMedium
        )
        Text(
            "Lexend",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    changeFont(context, activity, "lexend")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = LexendTypography.bodyMedium
        )
        Text(
            "Work Sans",
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .combinedClickable(onClick = {
                    changeFont(context, activity, "work")
                }),
            color = MaterialTheme.colorScheme.primary,
            style = WorkTypography.bodyMedium
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
                style = JostTypography.bodyMedium,
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