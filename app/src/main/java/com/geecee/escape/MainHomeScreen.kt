package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geecee.escape.ui.theme.EscapeTheme

class MainHomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            EscapeTheme {
                val packageManager = packageManager
                val context = LocalContext.current
                val navController = rememberNavController()


                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(onOpenAppDrawer = {
                                navController.navigate("app_drawer")
                            }, onOpenSettings = {
                                navController.navigate("settings")
                            })
                        }
                        composable("app_drawer") {
                            AppDrawer(packageManager, context, onCloseAppDrawer = {
                                navController.popBackStack()
                            })
                        }
                        composable("settings") {
                            Settings()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(onOpenAppDrawer: () -> Unit, onOpenSettings: () -> Unit) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .combinedClickable(onClick = {}, onLongClickLabel = {}.toString(), onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenSettings()
            })
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "TIME", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onOpenAppDrawer() },  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "app", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(140.dp))

            Button(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenAppDrawer()
            }) {
                Icon(Icons.Rounded.KeyboardArrowDown, "Open app drawer", tint = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(packageManager: PackageManager, context: Context, onCloseAppDrawer: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val installedApps = packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.GET_ACTIVITIES
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var currentSelectedApp by remember { mutableStateOf("") }
    var currentPackageName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(10.dp, 50.dp, 10.dp, 0.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(0.dp, 0.dp, 0.dp, 140.dp)
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "All Apps", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            installedApps.sortedBy { it.loadLabel(packageManager).toString() }.forEach { appInfo ->
                Text(
                    appInfo.loadLabel(packageManager).toString(),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                        .combinedClickable(onClick = {
                            val launchIntent =
                                packageManager.getLaunchIntentForPackage(appInfo.activityInfo.packageName)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                val options = ActivityOptions.makeCustomAnimation(
                                    context, R.anim.slide_in_bottom, R.anim.slide_out_top
                                )
                                context.startActivity(launchIntent, options.toBundle())
                            }
                        }, onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showBottomSheet = true
                            currentSelectedApp = appInfo
                                .loadLabel(packageManager)
                                .toString()
                            currentPackageName = appInfo.activityInfo.packageName
                        }),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )

            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 50.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Button(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onCloseAppDrawer()
        }) {
            Icon(Icons.Rounded.KeyboardArrowUp, "Open app drawer", tint = Color.Black)
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false; }, sheetState = sheetState) {
            Column(Modifier.padding(25.dp, 25.dp, 25.dp, 50.dp)) {
                Row {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "App Options",
                        tint = Color.White,
                        modifier = Modifier
                            .size(45.dp)
                            .padding(0.dp, 0.dp, 10.dp, 0.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        currentSelectedApp,
                        Modifier,
                        MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp
                    )
                }
                HorizontalDivider(Modifier.padding(0.dp, 15.dp))
                Column(Modifier.padding(47.dp, 0.dp, 0.dp, 0.dp)) {
                    Text(
                        "Uninstall",
                        Modifier
                            .padding(0.dp, 10.dp)
                            .combinedClickable(onClick = {
                                // Uninstall logic here
                                val intent =
                                    Intent(Intent.ACTION_DELETE, Uri.parse("package:$currentPackageName"))
                                Log.d("PACKAGE NAME","package:$currentPackageName")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)

                            }),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        "Hide",
                        Modifier.padding(0.dp, 10.dp),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        "Add Open Challenge",
                        Modifier.padding(0.dp, 10.dp),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                    Text(
                        "App Info",
                        Modifier.padding(0.dp, 10.dp),
                        MaterialTheme.colorScheme.primary,
                        fontSize = 25.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Settings() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(10.dp, 50.dp, 10.dp, 11.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "Settings", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Change Background Colour",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Edit Home Screen", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Home Alignment", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Apps Alignment", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Home Vertical Alignment",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Hidden Apps", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Open Challenges", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Font", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
                )
            }

            Button(
                onClick = {},  // Trigger opening drawer on button click
                modifier = Modifier.height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    "Make Default Launcher",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}