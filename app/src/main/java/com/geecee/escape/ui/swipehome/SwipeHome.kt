package com.geecee.escape

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.SwipeableState
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SwipeHome(context: Context, packageManager: PackageManager) {
    // Swipeable setup
    val appDrawSize = 1000.dp
    val swipeableState = rememberSwipeableState(1)
    val sizePx = with(LocalDensity.current) { appDrawSize.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1) // Maps anchor points (in px) to states
    val appsListScrollState = rememberLazyListState()
    val isAppDrawAtTop = remember {
        derivedStateOf { appsListScrollState.firstVisibleItemIndex == 0 && appsListScrollState.firstVisibleItemScrollOffset == 0 }
    }
    val shouldScroll = remember {mutableStateOf(true)}

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == 1) {
            shouldScroll.value = true
            println("Home screen is now in view")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Vertical,
                enabled = isAppDrawAtTop.value
            )
            .background(Color.LightGray)
    ) {


        SwipeHomeAppsList(
            context = context,
            packageManager = packageManager,
            swipeableState = swipeableState,
            scrollState = appsListScrollState,
            isAtTop = isAppDrawAtTop,
            shouldScroll = shouldScroll
        )
    }
}

@Composable
fun SwipeHomeHome() {

}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun SwipeHomeAppsList(
    context: Context,
    packageManager: PackageManager,
    swipeableState: SwipeableState<Int>,
    scrollState: LazyListState,
    isAtTop: State<Boolean>,
    shouldScroll: MutableState<Boolean>
) {
    val installedApps = AppUtils.getAllInstalledApps(packageManager = packageManager)
    val sortedInstalledApps =
        installedApps.sortedBy { getAppNameFromPackageName(context, it.activityInfo.packageName) }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp, 0.dp, 30.dp, 0.dp)
            .nestedScroll(remember {
                object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return if (available.y > 0 && isAtTop.value) {
                            shouldScroll.value = false
                            Offset.Zero
                        } else {
                            shouldScroll.value = true
                            super.onPreScroll(available, source)
                        }
                    }
                }
            }),
        userScrollEnabled = shouldScroll.value
    ) {
        item {
            Spacer(modifier = Modifier.height(140.dp))
            Text(
                text = stringResource(id = R.string.all_apps),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(sortedInstalledApps) { app ->
            if (app.activityInfo.packageName != "com.geecee.escape")
                SwipeAppsListItem(app, context = context, packageManager = packageManager)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeAppsListItem(app: ResolveInfo, packageManager: PackageManager, context: Context) {
    Text(
        AppUtils.getAppNameFromPackageName(context, app.activityInfo.packageName),
        modifier = Modifier
            .padding(vertical = 15.dp)
            .combinedClickable(
                onClick = {
                    val packageName = app.activityInfo.packageName

                    //TODO: Open Challenges

                    AppUtils.openApp(
                        packageManager = packageManager,
                        context = context,
                        packageName
                    )
                },
                onLongClick = {

                    //TODO: Bottom sheet

                }
            ),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium
    )
}
