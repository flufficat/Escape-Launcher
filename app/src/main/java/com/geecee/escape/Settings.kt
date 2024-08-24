package com.geecee.escape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsScreen() {
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
                    "Select Home Screen Widget", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp
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
