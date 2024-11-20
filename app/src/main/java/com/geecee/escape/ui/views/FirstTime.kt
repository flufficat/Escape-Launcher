package com.geecee.escape.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escape.R
import com.geecee.escape.ui.theme.JosefinTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FirstTime(onOpenSetup: () -> Unit) {
    var currentText by remember { mutableStateOf("Hi.") }
    var showText by remember { mutableStateOf(true) }

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

                delay(500)
                onOpenSetup()
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFAABB), // Peachy-pink color
                        Color(0xFFB19CD9)  // Soft lavender color
                    ),
                    start = Offset(0f, 0f),  // Starting point (top-left corner)
                    end = Offset(0f, Float.POSITIVE_INFINITY) // Ending point (bottom-center)
                )
            ),
        contentAlignment = Alignment.Center
    ) {


        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
        ) {
            Text(
                currentText,
                Modifier.padding(32.dp),
                Color.White,
                style = JosefinTypography.titleSmall,
                textAlign = TextAlign.Center,

                )
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)) {
            SkipButton {
                onOpenSetup()
            }
        }
    }
}

@Composable
fun SkipButton(onClick: () -> Unit) {
    Row(Modifier.clickable {
        onClick()
    }) {
        Text(
            stringResource(R.string.skip),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp
        )
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            "",
            Modifier.size(35.dp),
            MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
fun PrevSkipButton() {
    SkipButton { }
}