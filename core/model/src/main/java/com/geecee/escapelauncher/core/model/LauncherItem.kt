package com.geecee.escapelauncher.core.model

/**
 * Something that can appear in the app drawer, search results, or home-screen favourites: either
 * an installed app or a shortcut pinned by another app (e.g. Chrome's "Add to Home screen").
 */
sealed interface LauncherItem {
    val displayName: String

    /** Stable, collision-free key used for list diffing and favourite/order tracking. */
    val itemKey: String

    /** [TYPE_APP] or [TYPE_SHORTCUT], as persisted alongside favourite ordering. */
    val itemType: String

    data class App(val app: InstalledApp) : LauncherItem {
        override val displayName: String get() = app.displayName
        override val itemKey: String get() = "app:${app.packageName}"
        override val itemType: String get() = TYPE_APP
    }

    data class Shortcut(val shortcut: PinnedShortcut) : LauncherItem {
        override val displayName: String get() = shortcut.displayName
        override val itemKey: String get() = "shortcut:${shortcut.packageName}:${shortcut.shortcutId}"
        override val itemType: String get() = TYPE_SHORTCUT
    }

    /** String discriminators used to persist [itemKey] type alongside favourite ordering. */
    companion object {
        const val TYPE_APP = "app"
        const val TYPE_SHORTCUT = "shortcut"
    }
}
