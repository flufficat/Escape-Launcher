package com.geecee.escapelauncher.core.data.repository.shortcuts

import com.geecee.escapelauncher.core.data.database.PinnedShortcutsDao
import com.geecee.escapelauncher.core.data.entity.PinnedShortcutEntity
import com.geecee.escapelauncher.core.domain.repository.shortcuts.PinnedShortcutsRepository
import com.geecee.escapelauncher.core.model.PinnedShortcut
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinnedShortcutsRepositoryImpl @Inject constructor(
    private val pinnedShortcutsDao: PinnedShortcutsDao
) : PinnedShortcutsRepository {

    override fun getAllFlow(): Flow<List<PinnedShortcut>> =
        pinnedShortcutsDao.getAllFlow().map { entities -> entities.map { it.asExternalModel() } }

    override suspend fun getAll(): List<PinnedShortcut> =
        pinnedShortcutsDao.getAll().map { it.asExternalModel() }

    override suspend fun getAllForPackage(packageName: String): List<PinnedShortcut> =
        pinnedShortcutsDao.getAllForPackage(packageName).map { it.asExternalModel() }

    override suspend fun isPinned(packageName: String, shortcutId: String): Boolean =
        pinnedShortcutsDao.isPinned(packageName, shortcutId)

    override suspend fun upsert(shortcut: PinnedShortcut) {
        pinnedShortcutsDao.upsert(
            PinnedShortcutEntity(
                packageName = shortcut.packageName,
                shortcutId = shortcut.shortcutId,
                label = shortcut.label,
                customLabel = shortcut.customLabel
            )
        )
    }

    override suspend fun delete(packageName: String, shortcutId: String) {
        pinnedShortcutsDao.delete(packageName, shortcutId)
    }

    override suspend fun deleteAllForPackage(packageName: String) {
        pinnedShortcutsDao.deleteAllForPackage(packageName)
    }

    override suspend fun setCustomLabel(packageName: String, shortcutId: String, customLabel: String?) {
        pinnedShortcutsDao.updateCustomLabel(packageName, shortcutId, customLabel)
    }

    override suspend fun updateLabel(packageName: String, shortcutId: String, label: String) {
        pinnedShortcutsDao.updateLabel(packageName, shortcutId, label)
    }
}

fun PinnedShortcutEntity.asExternalModel() = PinnedShortcut(
    packageName = packageName,
    shortcutId = shortcutId,
    label = label,
    customLabel = customLabel
)
