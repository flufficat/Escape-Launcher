package com.geecee.escapelauncher.core.data.repository.db

import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.favourites.FavouritesRepository
import com.geecee.escapelauncher.core.model.LauncherItem
import com.geecee.escapelauncher.core.model.ModifiedApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private fun appItemKey(packageId: String) = "app:$packageId"

@Singleton
class ModifiedAppsRepositoryImpl @Inject constructor(
    private val modifiedAppsDao: ModifiedAppsDao,
    private val favouritesRepository: FavouritesRepository
) : ModifiedAppsRepository {
    override fun getHiddenPackageIdsFlow(): Flow<List<String>> =
        modifiedAppsDao.getHiddenPackageIdsFlow()

    override fun getChallengePackageIdsFlow(): Flow<List<String>> =
        modifiedAppsDao.getChallengePackageIdsFlow()

    override fun getFavouriteAppsInOrderFlow(): Flow<List<ModifiedApp>> =
        combine(
            favouritesRepository.getFavouriteOrderFlow(),
            modifiedAppsDao.getAllFlow()
        ) { order, apps ->
            mergeFavouriteApps(order.filter { it.itemType == LauncherItem.TYPE_APP }, apps)
        }

    override suspend fun getByPackageId(packageId: String): ModifiedApp? {
        return modifiedAppsDao.getByPackageId(packageId)?.asExternalModel()
    }

    override suspend fun setDisplayName(packageId: String, displayName: String?) {
        modifiedAppsDao.setDisplayName(packageId, displayName)
    }

    override suspend fun getDisplayName(packageId: String): String? {
        return modifiedAppsDao.getDisplayName(packageId)
    }

    override suspend fun clearDisplayName(packageId: String) {
        modifiedAppsDao.clearDisplayName(packageId)
    }

    override suspend fun setHidden(packageId: String, isHidden: Boolean) {
        modifiedAppsDao.setIsHidden(packageId, isHidden)
    }

    override suspend fun isHidden(packageId: String): Boolean {
        return modifiedAppsDao.isHidden(packageId)
    }

    override suspend fun setChallenge(packageId: String, isChallenge: Boolean) {
        modifiedAppsDao.setIsChallenge(packageId, isChallenge)
    }

    override suspend fun isChallenge(packageId: String): Boolean {
        return modifiedAppsDao.isChallenge(packageId)
    }

    override suspend fun setFavouritePosition(packageId: String, favouritePosition: Double?) {
        if (favouritePosition == null) {
            favouritesRepository.removeFavourite(appItemKey(packageId))
        } else {
            favouritesRepository.addFavourite(appItemKey(packageId), LauncherItem.TYPE_APP)
        }
    }

    override suspend fun getFavouritePosition(packageId: String): Double? {
        return favouritesRepository.getPosition(appItemKey(packageId))
    }

    override suspend fun clearFavouritePosition(packageId: String) {
        favouritesRepository.removeFavourite(appItemKey(packageId))
    }

    override suspend fun isFavourite(packageId: String): Boolean {
        return favouritesRepository.isFavourite(appItemKey(packageId))
    }

    override suspend fun addFavourite(packageId: String) {
        favouritesRepository.addFavourite(appItemKey(packageId), LauncherItem.TYPE_APP)
    }

    override suspend fun removeFavourite(packageId: String) {
        favouritesRepository.removeFavourite(appItemKey(packageId))
    }

    override suspend fun reorderFavouriteApp(packageId: String, fromIndex: Int, toIndex: Int) {
        favouritesRepository.reorderFavourite(
            itemKey = appItemKey(packageId),
            itemType = LauncherItem.TYPE_APP,
            fromIndex = fromIndex,
            toIndex = toIndex,
            scopeItemType = LauncherItem.TYPE_APP
        )
    }

    override suspend fun tidyFavouritePositions() {
        favouritesRepository.tidyFavouritePositions()
    }

    override suspend fun getFavouriteAppsInOrder(): List<ModifiedApp> {
        val order = favouritesRepository.getFavouriteOrder().filter { it.itemType == LauncherItem.TYPE_APP }
        val apps = modifiedAppsDao.getAllFlow().first()
        return mergeFavouriteApps(order, apps)
    }

    override suspend fun getHiddenPackageIds(): List<String> {
        return modifiedAppsDao.getHiddenPackageIds()
    }

    override suspend fun getChallengePackageIds(): List<String> {
        return modifiedAppsDao.getChallengePackageIds()
    }

    override suspend fun purgeAppsWithNoData(): Int {
        return modifiedAppsDao.purgeAppsWithNoData()
    }

    override suspend fun deleteByPackageId(packageId: String) {
        modifiedAppsDao.deleteByPackageId(packageId)
    }
}

private fun mergeFavouriteApps(
    appFavouriteOrder: List<com.geecee.escapelauncher.core.model.FavouriteOrder>,
    apps: List<ModifiedAppEntity>
): List<ModifiedApp> {
    val appsByPackageId = apps.associateBy { it.packageId }
    return appFavouriteOrder.map { entry ->
        val packageId = entry.itemKey.removePrefix("app:")
        val entity = appsByPackageId[packageId]
        ModifiedApp(
            packageId = packageId,
            displayName = entity?.displayName,
            isHidden = entity?.isHidden ?: false,
            isChallenge = entity?.isChallenge ?: false,
            favouritePosition = entry.position
        )
    }
}

fun ModifiedAppEntity.asExternalModel() = ModifiedApp(
    packageId = packageId,
    displayName = displayName,
    isHidden = isHidden,
    isChallenge = isChallenge,
    favouritePosition = favouritePosition
)
