package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModifiedAppsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(app: ModifiedAppEntity): Long

    @Update
    suspend fun updateAll(apps: List<ModifiedAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: ModifiedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(apps: List<ModifiedAppEntity>)

    @Query("SELECT * FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getByPackageId(packageId: String): ModifiedAppEntity?

    @Query("SELECT displayName FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getDisplayName(packageId: String): String?

    @Transaction
    suspend fun setDisplayName(packageId: String, displayName: String?) {
        val current = getByPackageId(packageId)
        if (current == null) {
            upsert(
                ModifiedAppEntity(
                    packageId = packageId,
                    displayName = displayName,
                    isHidden = false,
                    isChallenge = false,
                    favouritePosition = null
                )
            )
        } else {
            upsert(current.copy(displayName = displayName))
        }
    }

    @Transaction
    suspend fun clearDisplayName(packageId: String) {
        setDisplayName(packageId, null)
    }

    @Query("UPDATE modifiedApps SET isHidden = :isHidden WHERE packageId = :packageId")
    suspend fun updateIsHidden(packageId: String, isHidden: Boolean)

    @Query("SELECT isHidden FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getIsHidden(packageId: String): Boolean?

    @Query("UPDATE modifiedApps SET isChallenge = :isChallenge WHERE packageId = :packageId")
    suspend fun updateIsChallenge(packageId: String, isChallenge: Boolean)

    @Query("SELECT isChallenge FROM modifiedApps WHERE packageId = :packageId LIMIT 1")
    suspend fun getIsChallenge(packageId: String): Boolean?

    @Transaction
    suspend fun setIsHidden(packageId: String, isHidden: Boolean) {
        ensureRowExists(packageId)
        updateIsHidden(packageId, isHidden)
    }

    @Transaction
    suspend fun setIsChallenge(packageId: String, isChallenge: Boolean) {
        ensureRowExists(packageId)
        updateIsChallenge(packageId, isChallenge)
    }

    @Transaction
    suspend fun ensureRowExists(packageId: String) {
        insertIfMissing(
            ModifiedAppEntity(
                packageId = packageId,
                displayName = null,
                isHidden = false,
                isChallenge = false,
                favouritePosition = null
            )
        )
    }

    @Query("SELECT * FROM modifiedApps")
    fun getAllFlow(): Flow<List<ModifiedAppEntity>>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isHidden = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    fun getHiddenPackageIdsFlow(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isChallenge = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    fun getChallengePackageIdsFlow(): kotlinx.coroutines.flow.Flow<List<String>>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isHidden = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    suspend fun getHiddenPackageIds(): List<String>

    @Query(
        """
        SELECT packageId
        FROM modifiedApps
        WHERE isChallenge = 1
        ORDER BY COALESCE(displayName, packageId) COLLATE NOCASE ASC
        """
    )
    suspend fun getChallengePackageIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageId = :packageId AND isHidden = 1)")
    suspend fun isHidden(packageId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM modifiedApps WHERE packageId = :packageId AND isChallenge = 1)")
    suspend fun isChallenge(packageId: String): Boolean

    @Query("DELETE FROM modifiedApps WHERE packageId = :packageId")
    suspend fun deleteByPackageId(packageId: String)

    // favouritePosition is deliberately excluded from this check: it's a vestigial column left
    // over from before favourites moved to the shared favouriteOrder table (see Migrations.kt)
    // and is no longer read or written, so a stale non-null value here shouldn't block purging
    // an otherwise-empty row.
    @Query(
        """
        DELETE FROM modifiedApps
        WHERE displayName IS NULL
          AND isHidden = 0
          AND isChallenge = 0
        """
    )
    suspend fun purgeAppsWithNoData(): Int
}