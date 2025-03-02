package com.geecee.escapelauncher.utils.managers

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.work.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object ScreenTimeManager {
    private val appSessions = ConcurrentHashMap<String, Long>() // Thread-safe in-memory tracking
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

        val appKey = "$packageName-$currentDate"  // Include date in the package name key

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = database.appUsageDao()
                val existingUsage = dao.getAppUsage(appKey)
                val updatedTime = (existingUsage?.totalTime ?: 0L) + usageTime

                dao.insertOrUpdate(
                    AppUsageEntity(
                        packageName = appKey,
                        totalTime = updatedTime
                    )
                )
                appSessions.remove(packageName)
            } catch (e: Exception) {
                Log.e("ScreenTimeManager", "Error saving app usage: ${e.message}")
            }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun clearOldData() {
        val retentionThreshold: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                database.appUsageDao().clearOldData("%$retentionThreshold%")
            } catch (e: Exception) {
                Log.e("ScreenTimeManager", "Error clearing old data: ${e.message}")
            }
        }
    }
}

// The database
@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,  // now includes the date as part of the package name
    val totalTime: Long
)

@Database(entities = [AppUsageEntity::class], version = 1, exportSchema = false)
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

// Database DAO
@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appUsage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName")
    suspend fun getAppUsage(packageName: String): AppUsageEntity?

    @Query("DELETE FROM app_usage WHERE packageName LIKE :packageNamePrefix")
    suspend fun clearOldData(packageNamePrefix: String)

    @Query("SELECT * FROM app_usage")
    suspend fun getAllUsage(): List<AppUsageEntity>
}


// Calculates delay until next midnight
private fun calculateMidnightDelay(): Long {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar.timeInMillis - now
}

// Utility functions
suspend fun getTotalUsageForDate(date: String): Long {
    val dao = ScreenTimeManager.database.appUsageDao()
    val allUsage = dao.getAllUsage() // Fetch all app usage data

    // Sum the total time for packageNames that end with the specified date
    return allUsage.filter {
        it.packageName.endsWith("-$date") // Check if packageName ends with "-yyyy-MM-dd"
    }.sumOf { it.totalTime } // Sum the totalTime
}

suspend fun getUsageForApp(packageName: String, date: String): Long {
    val dao = ScreenTimeManager.database.appUsageDao()
    return dao.getAppUsage("$packageName-$date")?.totalTime ?: 0L
}

suspend fun getScreenTimeListSorted(date: String): List<AppUsageEntity> {
    val dao = ScreenTimeManager.database.appUsageDao()
    val allUsage = dao.getAllUsage() // Fetch all app usage data

    // Filter and transform to strip date and dash from packageName
    return allUsage.filter {
        it.packageName.endsWith("-$date") // Check if packageName ends with "-yyyy-MM-dd"
    }.map { usage ->
        // Create a new AppUsageEntity with the packageName stripped of the date and dash
        AppUsageEntity(
            packageName = usage.packageName.substringBeforeLast("-$date"),
            totalTime = usage.totalTime
        )
    }.sortedByDescending { it.totalTime } // Sort by totalTime in descending order
}


// Clear redundant data
class ClearOldDataWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            ScreenTimeManager.clearOldData()
            Result.success()
        } catch (e: Exception) {
            Log.e("ClearOldDataWorker", "Error clearing old data: ${e.message}")
            Result.failure()
        }
    }
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