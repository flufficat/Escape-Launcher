package com.geecee.escapelauncher.core.model

/**
 * A shortcut pinned to this launcher by another app (e.g. Chrome's "Add to Home screen", a Maps
 * saved location, or a dedicated shortcut-creation app), persisted so it can appear in the app
 * drawer/search and optionally be favourited onto the home screen like a regular app.
 *
 * Distinct from [AppShortcut], which is an ephemeral, unpersisted peek at an app's own
 * dynamic/manifest shortcuts shown inside that app's long-press menu.
 */
data class PinnedShortcut(
    val packageName: String,
    val shortcutId: String,
    val label: String,
    val customLabel: String? = null
) {
    val displayName: String get() = customLabel ?: label
}
