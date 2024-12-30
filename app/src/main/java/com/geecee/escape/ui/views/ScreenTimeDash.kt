package com.geecee.escape.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geecee.escape.ui.theme.EscapeTheme
import com.geecee.escape.ui.theme.escapeGreen
import com.geecee.escape.ui.theme.escapeRed

@Composable
fun ScreenTimeDashboard() {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(15.dp, 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(120.dp))

        ScreenTime(
            "3h 25m", false, Modifier
        )

        Spacer(Modifier.height(15.dp))

        Row(
            Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            DaySpent(
                12,
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            GovRecommend(
                5,
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }

        Spacer(Modifier.height(15.dp))

        AppUsages(Modifier) {
            AppUsage("Instagram", true, "1h 34m", Modifier)
            AppUsage("Minecraft", false, "1h 12m", Modifier)
            AppUsage("X, the everything app", true, "1h 4m", Modifier)
            AppUsage("BlueSky", false, "48m", Modifier)
            AppUsage("Youtube", false, "34m", Modifier)
        }

        Spacer(Modifier.height(15.dp))
    }
}

@Composable
fun ScreenTime(time: String, increased: Boolean, modifier: Modifier) {
    Row {
        Icon(
            Icons.Default.KeyboardArrowUp, contentDescription = "Arrow", tint = if (increased) {
                escapeRed
            } else {
                escapeGreen
            }, modifier = Modifier
                .size(45.dp)
                .align(Alignment.CenterVertically)
                .rotate(
                    if (increased) {
                        0f
                    } else {
                        180f
                    }
                )
        )

        Spacer(Modifier.width(5.dp))


        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

    }
}

@Composable
fun GovRecommend(percent: Int, modifier: Modifier) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$percent%",
                style = MaterialTheme.typography.titleMedium,
                color = if (percent < 1) {
                    escapeGreen
                } else {
                    escapeRed
                },
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Higher than the UK recommends",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DaySpent(percent: Int, modifier: Modifier) {
    Box(
        modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$percent%",
                style = MaterialTheme.typography.titleMedium,
                color = if (percent < 10) {
                    escapeGreen
                } else {
                    escapeRed
                },
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Of your day spent on your phone",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AppUsage(appName: String, increased: Boolean, time: String, modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp)) {
        Text(
            text = if (appName.length > 12) appName.take(12) + "..." else appName,
            modifier = Modifier.align(Alignment.CenterStart),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Row(
            Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp, contentDescription = "Arrow", tint = if (increased) {
                    escapeRed
                } else {
                    escapeGreen
                }, modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.CenterVertically)
                    .rotate(
                        if (increased) {
                            0f
                        } else {
                            180f
                        }
                    )
            )

            Spacer(Modifier.width(5.dp))


            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AppUsages(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

// = = = = = = = = = = = = = = = = //
//             Previews            //
// = = = = = = = = = = = = = = = = //

@Composable
@Preview
fun ScreenTimePrevDec() {
    EscapeTheme {
        ScreenTime("3h 24m", false, Modifier)
    }
}

@Composable
@Preview
fun ScreenTimePrev() {
    EscapeTheme {
        ScreenTime("3h 24m", true, Modifier)
    }
}

@Composable
@Preview
fun ScreenTimeDashPreview() {
    EscapeTheme {
        ScreenTimeDashboard()
    }
}

@Composable
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
fun ScreenTimeDashPreviewLandscape() {
    EscapeTheme {
        ScreenTimeDashboard()
    }
}

@Composable
@Preview
fun DavSpentPrev() {
    EscapeTheme {
        DaySpent(
            20,
            Modifier
                .size(200.dp)
                .aspectRatio(1f)
        )
    }
}

@Composable
@Preview
fun DavSpentBelowTenPrev() {
    EscapeTheme {
        DaySpent(
            1,
            Modifier
                .size(200.dp)
                .aspectRatio(1f)
        )
    }
}

@Composable
@Preview
fun GovRecommendPrev() {
    EscapeTheme {
        GovRecommend(
            20,
            Modifier
                .size(200.dp)
                .aspectRatio(1f)
        )
    }
}

@Composable
@Preview
fun GovRecommendBelowTenPrev() {
    EscapeTheme {
        GovRecommend(
            0,
            Modifier
                .size(200.dp)
                .aspectRatio(1f)
        )
    }
}

@Composable
@Preview
fun AppUsagesPrev() {
    EscapeTheme {
        AppUsages(Modifier) {
            AppUsage("Instagram", true, "1h 34m", Modifier)
        }
    }
}

@Composable
@Preview
fun AppUsagePrev() {
    EscapeTheme {
        AppUsage("Instagram", true, "1h 43m", Modifier)
    }
}