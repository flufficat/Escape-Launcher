package com.geecee.escape.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.geecee.escape.MainAppModel
import com.geecee.escape.R
import com.geecee.escape.ui.theme.JostTypography
import com.geecee.escape.utils.AppUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//Apps list from the pager
@Composable
fun AppsList(
    mainAppModel: MainAppModel,
    homeScreenModel: HomeScreenModel
) {
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = homeScreenModel.appsListScrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp, 0.dp),
            horizontalAlignment = AppUtils.getAppsListAlignmentFromPreferences(
                homeScreenModel.sharedPreferences
            )
        ) {
            item {
                Spacer(modifier = Modifier.height(140.dp))
                Text(
                    text = stringResource(id = R.string.all_apps),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                if (homeScreenModel.sharedPreferences.getBoolean("showSearchBox", true)) {
                    Spacer(modifier = Modifier.height(15.dp))
                    AnimatedPillSearchBar(
                        { searchBoxText ->
                            homeScreenModel.searchText.value = searchBoxText
                            var autoOpen = false

                            if (homeScreenModel.sharedPreferences.getBoolean(
                                    "searchAutoOpen",
                                    false
                                )
                            ) {
                                autoOpen = true
                            }

                            if (autoOpen) {
                                if (AppUtils.filterAndSortApps(
                                        homeScreenModel.installedApps,
                                        homeScreenModel.searchText.value,
                                        mainAppModel.packageManager
                                    ).size == 1
                                ) {
                                    val appInfo = AppUtils.filterAndSortApps(
                                        homeScreenModel.installedApps,
                                        homeScreenModel.searchText.value,
                                        mainAppModel.packageManager
                                    ).first()
                                    homeScreenModel.currentPackageName.value =
                                        appInfo.activityInfo.packageName

                                    AppUtils.openApp(
                                        homeScreenModel.currentPackageName.value,
                                        false,
                                        homeScreenModel.showOpenChallenge,
                                        mainAppModel
                                    )

                                    homeScreenModel.coroutineScope.launch {
                                        delay(200)
                                        homeScreenModel.pagerState.animateScrollToPage(1)
                                        homeScreenModel.appsListScrollState.scrollToItem(0)
                                        homeScreenModel.searchExpanded.value = false
                                        homeScreenModel.searchText.value = ""
                                    }

                                }
                            }
                        },
                        { searchBoxText ->
                            if (AppUtils.filterAndSortApps(
                                    homeScreenModel.installedApps,
                                    searchBoxText,
                                    mainAppModel.packageManager
                                ).isNotEmpty()
                            ) {
                                val firstAppInfo = AppUtils.filterAndSortApps(
                                    homeScreenModel.installedApps,
                                    searchBoxText,
                                    mainAppModel.packageManager
                                ).first()

                                val packageName = firstAppInfo.activityInfo.packageName
                                homeScreenModel.currentPackageName.value = packageName

                                AppUtils.openApp(
                                    homeScreenModel.currentPackageName.value,
                                    false,
                                    homeScreenModel.showOpenChallenge,
                                    mainAppModel
                                )

                                homeScreenModel.coroutineScope.launch {
                                    delay(200)
                                    homeScreenModel.pagerState.animateScrollToPage(1)
                                    homeScreenModel.appsListScrollState.scrollToItem(0)
                                    homeScreenModel.searchExpanded.value = false
                                    homeScreenModel.searchText.value = ""
                                }
                            }
                        },
                        homeScreenModel.searchExpanded
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            items(homeScreenModel.sortedInstalledApps.filter { appInfo ->
                val appName = appInfo.loadLabel(mainAppModel.packageManager).toString()
                appName.contains(homeScreenModel.searchText.value, ignoreCase = true)
            })
            { app ->
                if (app.activityInfo.packageName != "com.geecee.escape" && !mainAppModel.hiddenAppsManager.isAppHidden(
                        app.activityInfo.packageName
                    )
                )
                    AppsListItem(
                        app,
                        mainAppModel = mainAppModel,
                        homeScreenModel = homeScreenModel,
                        lazyListState = homeScreenModel.appsListScrollState,
                        searchExpanded = homeScreenModel.searchExpanded,
                        searchText = homeScreenModel.searchText,
                    )
            }


            item {
                Spacer(modifier = Modifier.height(90.dp))
            }

            //TODO: Private space
//            item {
//                val isPrivateSpaceVisible by remember { mutableStateOf(false) }
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    Button(onClick = {
//                        if(isPrivateSpace(mainAppModel.context)) {
//                            // Hide private space
//                            lockPrivateSpace(mainAppModel.context)
//                        } else {
//                            // Show private space
//                            unlockPrivateSpace(mainAppModel.context)
//                        }
//                    }) {
//                        Text(if (isPrivateSpaceVisible) "Hide Private Space" else "Show Private Space")
//                    }
//                }
//            }
        }
    }
}

//Search bar in the apps list
@Composable
fun AnimatedPillSearchBar(
    textChange: (searchText: String) -> Unit,
    keyboardDone: (searchText: String) -> Unit,
    expanded: MutableState<Boolean>
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }


    // Animate the width of the search bar
    val width by animateDpAsState(targetValue = if (expanded.value) 280.dp else 150.dp, label = "")

    // Animate the alpha of the text field content
    val alpha by animateFloatAsState(targetValue = if (expanded.value) 1f else 0f, label = "")

    // FocusRequester to request focus on the text field
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded.value) {
        if (expanded.value) {
            searchText = TextFieldValue("")
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Surface(
        modifier = Modifier
            .width(width)
            .height(56.dp)
            .clickable {
                expanded.value = !expanded.value
            }
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.onBackground
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .animateContentSize()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .padding(5.dp, 0.dp)
                    .size(25.dp)
            )

            if (!expanded.value) {
                Text(
                    stringResource(id = R.string.search),
                    modifier = Modifier.animateContentSize(),
                    color = MaterialTheme.colorScheme.background,
                    style = JostTypography.bodyMedium
                )
            }

            if (expanded.value) {
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                        .animateContentSize()
                )

                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        textChange(searchText.text)
                    },
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .animateContentSize(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (alpha > 0) {
                            innerTextField()
                        }
                    },
                    maxLines = 1,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            keyboardDone(searchText.text)
                        }
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}