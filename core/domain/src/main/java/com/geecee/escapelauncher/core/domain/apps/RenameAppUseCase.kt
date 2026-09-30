package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import jakarta.inject.Inject

/**
 * Renames an app, or clears the rename override (reverting to its real label) when [newName]
 * is blank.
 */
class RenameAppUseCase @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    suspend operator fun invoke(packageId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) {
            modifiedAppsRepository.clearDisplayName(packageId)
        } else {
            modifiedAppsRepository.setDisplayName(packageId, trimmed)
        }
    }
}
