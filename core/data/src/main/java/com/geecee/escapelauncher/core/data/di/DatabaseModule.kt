package com.geecee.escapelauncher.core.data.di

import android.content.Context
import androidx.room.Room
import com.geecee.escapelauncher.core.data.database.AppDatabase
import com.geecee.escapelauncher.core.data.database.AppUsageDao
import com.geecee.escapelauncher.core.data.database.FavouriteOrderDao
import com.geecee.escapelauncher.core.data.database.MIGRATION_1_2
import com.geecee.escapelauncher.core.data.database.MIGRATION_2_3
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDatabase
import com.geecee.escapelauncher.core.data.database.PinnedShortcutsDao
import com.geecee.escapelauncher.core.data.database.RelatedItemsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_usage_database"
        ).build()
    }

    @Provides
    fun provideAppUsageDao(database: AppDatabase): AppUsageDao {
        return database.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideModifiedAppsDatabase(
        @ApplicationContext context: Context
    ): ModifiedAppsDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ModifiedAppsDatabase::class.java,
            "modified_apps_database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }

    @Provides
    fun provideModifiedAppsDao(database: ModifiedAppsDatabase): ModifiedAppsDao {
        return database.modifiedAppsDao()
    }

    @Provides
    fun providePinnedShortcutsDao(database: ModifiedAppsDatabase): PinnedShortcutsDao {
        return database.pinnedShortcutsDao()
    }

    @Provides
    fun provideFavouriteOrderDao(database: ModifiedAppsDatabase): FavouriteOrderDao {
        return database.favouriteOrderDao()
    }

    @Provides
    fun provideRelatedItemsDao(database: ModifiedAppsDatabase): RelatedItemsDao {
        return database.relatedItemsDao()
    }
}
