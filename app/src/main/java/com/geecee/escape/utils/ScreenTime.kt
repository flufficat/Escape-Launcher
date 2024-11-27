@file:Suppress("unused")

package com.geecee.escape.utils

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ScreenTimeManager {
    private val appSessions = mutableMapOf<String, Long>() // In-memory tracking of app open times

    // Store usage in Room database
    lateinit var database: AppDatabase

    fun initialize(context: Context) {
        database = AppDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            clearOldData()
        }
    }

    // Called when an app is opened
    fun onAppOpened(packageName: String) {
        appSessions[packageName] = System.currentTimeMillis()
    }

    // Called when an app is closed
    fun onAppClosed(packageName: String) {
        val openTime = appSessions[packageName] ?: return
        val usageTime = System.currentTimeMillis() - openTime
        val currentDate = getCurrentDate()

        // Get the current usage for this app and date
        val dao = database.appUsageDao()
        val existingUsage = dao.getAppUsage(packageName, currentDate)
        val updatedTime = (existingUsage?.totalTime ?: 0L) + usageTime

        // Update or insert the usage data
        dao.insertOrUpdate(
            AppUsageEntity(
                packageName = packageName,
                totalTime = updatedTime,
                date = currentDate
            )
        )

        // Remove the app from in-memory tracking
        appSessions.remove(packageName)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun clearOldData() {
        val currentDate = getCurrentDate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                database.appUsageDao().clearOldData(currentDate)
            } catch (e: Exception) {
                Log.e("ScreenTimeManager", "Error clearing old data: ${e.message}")
            }
        }
    }

}

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val totalTime: Long,
    val date: String
)

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(appUsage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date")
    fun getAppUsage(packageName: String, date: String): AppUsageEntity?

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName")
    fun getAppUsageWithoutDate(packageName: String): AppUsageEntity?

    @Query("DELETE FROM app_usage WHERE date != :date")
    fun clearOldData(date: String)

    @Query("SELECT * FROM app_usage WHERE date = :date")
    fun getAllUsageForDate(date: String): List<AppUsageEntity>
}

@Database(entities = [AppUsageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_usage_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

fun getTotalUsageForDate(date: String): Long {
    val dao = ScreenTimeManager.database.appUsageDao()
    val usageList = dao.getAllUsageForDate(date) // Create a query for this
    return usageList.sumOf { it.totalTime }
}

fun getUsageForApp(packageName: String, date: String): Long {
    val dao = ScreenTimeManager.database.appUsageDao()
    return dao.getAppUsage(packageName, date)?.totalTime ?: 0L
}

fun getUsageForAppWithoutDate(packageName: String): Long {
    val dao = ScreenTimeManager.database.appUsageDao()
    return dao.getAppUsageWithoutDate(packageName)?.totalTime ?: 0L
}

fun saveAppUsageToDatabase(packageName: String, usageTime: Long, context: Context) {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dao = AppDatabase.getDatabase(context).appUsageDao()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Fetch existing usage data for the app on the current date
            val existingUsage = dao.getAppUsage(packageName, date)

            // Add the new usage time to the existing total time
            val updatedTime = (existingUsage?.totalTime ?: 0L) + usageTime

            // Create or update the usage data in the database
            dao.insertOrUpdate(
                AppUsageEntity(
                    packageName = packageName,
                    totalTime = updatedTime,
                    date = date
                )
            )
        } catch (e: Exception) {
            Log.e("ScreenTimeManager", "Error saving app usage: ${e.message}")
        }
    }
}

class ClearOldDataWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            ScreenTimeManager.clearOldData() // Clear old data using a coroutine-friendly method
            Result.success()
        } catch (e: Exception) {
            Log.e("ClearOldDataWorker", "Error clearing old data: ${e.message}")
            Result.failure()
        }
    }
}

private fun calculateMidnightDelay(): Long {
    val now = System.currentTimeMillis()
    val tomorrowMidnight = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .parse(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now + 24 * 60 * 60 * 1000)))!!
        .time
    return tomorrowMidnight - now
}


fun scheduleDailyCleanup(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<ClearOldDataWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateMidnightDelay(), TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "ClearOldDataWorker",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}