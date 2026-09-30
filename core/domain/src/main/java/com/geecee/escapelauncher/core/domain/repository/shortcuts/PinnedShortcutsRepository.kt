package com.geecee.escapelauncher.core.domain.repository.shortcuts

import com.geecee.escapelauncher.core.model.PinnedShortcut
import kotlinx.coroutines.flow.Flow

/**
 * Local persistence for shortcuts pinned to this launcher via [PinItemRequest][android.content.pm.LauncherApps.PinItemRequest].
 * This only tracks what the launcher has recorded locally; the OS-level pinned set (which
 * [android.content.pm.LauncherApps.pinShortcuts] replaces wholesale, not additively) is owned by
 * [com.geecee.escapelauncher.core.domain.repository.android.AppsRepository].
 */
interface PinnedShortcutsRepository {
    fun getAllFlow(): Flow<List<PinnedShortcut>>
    suspend fun getAll(): List<PinnedShortcut>
    suspend fun getAllForPackage(packageName: String): List<PinnedShortcut>
    suspend fun isPinned(packageName: String, shortcutId: String): Boolean
    suspend fun upsert(shortcut: PinnedShortcut)
    suspend fun delete(packageName: String, shortcutId: String)
    suspend fun deleteAllForPackage(packageName: String)
    suspend fun setCustomLabel(packageName: String, shortcutId: String, customLabel: String?)
    suspend fun updateLabel(packageName: String, shortcutId: String, label: String)
}
