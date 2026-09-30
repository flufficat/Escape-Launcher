package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import jakarta.inject.Inject

/**
 * Renames a pinned shortcut, or clears the rename override (reverting to the label the source
 * app published) when [newName] is blank.
 */
class RenameShortcutUseCase @Inject constructor(
    private val pinnedShortcutsRepository: PinnedShortcutsRepository
) {
    suspend operator fun invoke(packageName: String, shortcutId: String, newName: String) {
        val trimmed = newName.trim()
        pinnedShortcutsRepository.setCustomLabel(packageName, shortcutId, trimmed.ifBlank { null })
    }
}
