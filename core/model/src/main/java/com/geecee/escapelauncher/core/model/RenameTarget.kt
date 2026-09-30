package com.geecee.escapelauncher.core.model

/**
 * Identity + current label for whatever [LauncherItem] a rename dialog is open for, decoupling
 * the dialog from the exact shape of [LauncherItem] (it only needs a label to prefill and pass
 * back on save).
 */
sealed class RenameTarget {
    abstract val currentLabel: String

    data class App(val packageId: String, override val currentLabel: String) : RenameTarget()

    data class Shortcut(
        val packageName: String,
        val shortcutId: String,
        override val currentLabel: String
    ) : RenameTarget()
}
