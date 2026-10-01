package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject

sealed class AppActionType {
    object Uninstall : AppActionType()
    data class ToggleFavorite(val isFavorite: Boolean) : AppActionType()
    object Hide : AppActionType()
    object AppInfo : AppActionType()
    object AddChallenge : AppActionType()
    object RemoveShortcut : AppActionType()
    object Rename : AppActionType()
    object ManageRelatedItems : AppActionType()
}

class GetAppActionsUseCase @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val favouritesRepository: FavouritesRepository
) {
    operator fun invoke(item: LauncherItem): Flow<List<AppActionType>> {
        return when (item) {
            is LauncherItem.Shortcut -> favouritesRepository.getFavouriteOrderFlow().map { order ->
                val isFavorite = order.any { it.itemKey == item.itemKey }
                listOf(
                    AppActionType.ToggleFavorite(isFavorite),
                    AppActionType.Rename,
                    AppActionType.ManageRelatedItems,
                    AppActionType.RemoveShortcut
                )
            }

            is LauncherItem.App -> {
                val packageId = item.app.packageName
                combine(
                    favouritesRepository.getFavouriteOrderFlow(),
                    modifiedAppsRepository.getChallengePackageIdsFlow()
                ) { order, challenges ->
                    val isFavorite = order.any { it.itemKey == item.itemKey }
                    val hasChallenge = challenges.any { it == packageId }

                    buildList {
                        add(AppActionType.Uninstall)
                        add(AppActionType.ToggleFavorite(isFavorite))
                        add(AppActionType.Hide)
                        add(AppActionType.Rename)
                        add(AppActionType.ManageRelatedItems)
                        add(AppActionType.AppInfo)
                        if (!hasChallenge) {
                            add(AppActionType.AddChallenge)
                        }
                    }
                }
            }
        }
    }
}
