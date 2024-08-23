package com.geecee.escape

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                            })
                        }
                        composable("app_drawer") {
                            AppDrawer(packageManager, context, onCloseAppDrawer = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onOpenAppDrawer: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        .padding(10.dp, 50.dp, 10.dp, 11.dp)) {
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

            Button(onClick = { onOpenAppDrawer() }) {
                Icon(Icons.Rounded.KeyboardArrowDown, "Open app drawer")
            }
        }
    }
}

@Composable
fun AppDrawer(packageManager: PackageManager, context: Context, onCloseAppDrawer: () -> Unit) {

    val installedApps = packageManager.queryIntentActivities(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        PackageManager.GET_ACTIVITIES
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        .padding(10.dp, 50.dp, 10.dp, 11.dp))  {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = "All Apps", color = MaterialTheme.colorScheme.primary, fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            installedApps.sortedBy { it.loadLabel(packageManager).toString() }.forEach { appInfo ->
                Button(
                    onClick = {
                        val launchIntent =
                            packageManager.getLaunchIntentForPackage(appInfo.activityInfo.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            val options = ActivityOptions.makeCustomAnimation(
                                context, R.anim.slide_in_bottom, R.anim.slide_out_top
                            )
                            context.startActivity(launchIntent, options.toBundle())
                        }
                    },
                    modifier = Modifier.height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        appInfo.loadLabel(packageManager).toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
    Row(modifier = Modifier.fillMaxSize().padding(0.dp,50.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
        Button(onClick = { onCloseAppDrawer() }) {
            Icon(Icons.Rounded.KeyboardArrowUp, "Open app drawer")
        }
    }
}
