package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Search Bar for apps list that collapses into a pill
 */
@Composable
fun AnimatedPillSearchBar(
    searchText: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String, SoftwareKeyboardController?) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false
) {
    val scope = rememberCoroutineScope()

    var textFieldValue by remember { mutableStateOf(TextFieldValue(searchText)) }

    // Sync internal state with external searchText
    LaunchedEffect(searchText) {
        if (searchText != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = searchText)
        }
    }

    // Animation Specs
    // Matches the gesture-driven app-list page's own slide-in duration/easing (see
    // MainPagerScreenViewModel) so the pill's expansion and the list's entrance land at the same
    // moment instead of settling independently on their own timelines.
    val width by animateDpAsState(
        targetValue = if (isExpanded) 280.dp else 56.dp,
        label = "widthAnimation",
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
    )
    val interactionSource =  remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isMorphed by remember { mutableStateOf(false) }
    val radius = if (isMorphed || isPressed) 16.dp else 28.dp
    val animatedRadius by animateDpAsState(
        targetValue = radius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow
        ),
        label = "SearchBoxCornerAnimation"
    )

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Fades the text field in/out without ever removing it from composition (see below) - kept
    // close to the old AnimatedVisibility's 300ms fade for the same felt speed.
    val textFieldAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "textFieldAlpha"
    )

    // Handle Auto-focus and Expansion changes. No artificial delay before requesting focus -
    // the text field below is always composed (never gated behind AnimatedVisibility, which
    // would otherwise only create it - and lay it out - starting on this very same frame),
    // so by the time this fires it's already a real, measured node that can take focus
    // immediately, letting the keyboard start appearing in step with the list's own slide-in
    // instead of visibly trailing behind it.
    LaunchedEffect(isExpanded, autoFocus) {
        if (isExpanded) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Surface(
        shape = RoundedCornerShape(animatedRadius),
        modifier = modifier
            .width(width)
            .height(56.dp)
            .combinedClickable(
                interactionSource = interactionSource
            ) {
                if (!isMorphed) {
                    isMorphed = true
                    scope.launch {
                        delay(200.milliseconds)
                        isMorphed = false
                    }
                } else {
                    isMorphed = false
                }

                onExpandedChange(!isExpanded)
            },
        color = MaterialTheme.colorScheme.secondary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(24.dp)
            )

            BasicTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onSearchTextChanged(it.text)
                },
                // Always composed (unlike the old AnimatedVisibility-wrapped version) so
                // focusRequester always targets a real, already-laid-out node - only its
                // visibility and interactivity are toggled, not its presence in the tree.
                enabled = isExpanded,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 16.dp)
                    .alpha(textFieldAlpha)
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onSearchDone(textFieldValue.text.trim(), keyboardController)
                }),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondary
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSecondary),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        innerTextField()
                    }
                })
        }
    }
}