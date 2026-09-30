package com.geecee.escapelauncher.core.model

/**
 * Which gesture opens search from the home screen. Whichever direction isn't chosen falls back
 * to its own default behavior: [DOWN] opens the notification shade, [UP] and [LEFT] do nothing.
 */
enum class SearchGestureDirection {
    DOWN,
    UP,
    LEFT
}
