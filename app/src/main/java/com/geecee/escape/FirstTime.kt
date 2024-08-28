package com.geecee.escape

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FirstTime(onOpenSetup: () -> Unit) {
    var currentText by remember { mutableStateOf("Hi.") }
    var showText by remember { mutableStateOf(true) }
    var nextScreen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val stringOne = stringResource(id = R.string.average_screen_time)
    val stringTwo = stringResource(id = R.string.three_days)
    val stringThree = stringResource(id = R.string.escape_change)

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (true) {
                delay(3000)
                showText = false

                delay(1000)
                currentText = stringOne
                showText = true

                delay(3000)
                showText = false

                delay(1000)
                currentText = stringTwo
                showText = true

                delay(3000)
                showText = false

                delay(1000)
                currentText = stringThree
                showText = true

                delay(3000)
                showText = false
                delay(1000)
                nextScreen = true

                delay(500)
                onOpenSetup()
            }
        }
    }

    if (!nextScreen) {
        Box(
            Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.verticalbackground),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                exit = fadeOut(animationSpec = tween(durationMillis = 1000))
            ) {
                Text(
                    currentText,
                    Modifier.padding(16.dp),
                    Color.White,
                    fontSize = 32.sp,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        Box(
            Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Second Box with custom animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 1000)
                ),
                exit = fadeOut(
                    animationSpec = tween(durationMillis = 1000)
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.verticalbackground),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}