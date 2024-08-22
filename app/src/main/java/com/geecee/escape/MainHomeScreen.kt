package com.geecee.escape

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.ui.theme.EscapeTheme


class MainHomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EscapeTheme {
                val packageManager = packageManager
                val installedApps = packageManager.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                    PackageManager.GET_ACTIVITIES
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(10.dp, 50.dp, 10.dp, 11.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        val context = LocalContext.current


                        Spacer(modifier = Modifier.height(140.dp))

                        Text(
                            text = "TIME",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 48.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))


                        Button(
                            onClick = {
                                // Launch the app
                                val i = Intent(this@MainHomeScreen, AllAppsScreen::class.java)
                                startActivity(i)
                            },
                            modifier = Modifier.height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Text(
                                "An app",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                        }

                        Button(
                            onClick = {
                                // Launch the app
                            },
                            modifier = Modifier.height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Text(
                                "Another app",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                        }

                        Button(
                            onClick = {
                                // Launch the app
                            },
                            modifier = Modifier.height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Text(
                                "A final app",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            )
                        }


                        Spacer(modifier = Modifier.height(140.dp))
                    }
                }
            }
        }
    }
}
