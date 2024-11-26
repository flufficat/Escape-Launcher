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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScreenTimeManager {
    private val appSessions = mutableMapOf<String, Long>() // In-memory tracking of app open times

    // Store usage in Room database
    lateinit var database: AppDatabase

    fun initialize(context: Context) {
        database = AppDatabase.getDatabase(context)
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
        dao.insertOrUpdate(AppUsageEntity(packageName = packageName, totalTime = updatedTime, date = currentDate))

        // Remove the app from in-memory tracking
        appSessions.remove(packageName)
    }

    // Clear old data (e.g., at midnight)
    fun clearOldData() {
        val currentDate = getCurrentDate()
        database.appUsageDao().clearOldData(currentDate)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
        @Volatile private var INSTANCE: AppDatabase? = null

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
            dao.insertOrUpdate(AppUsageEntity(packageName = packageName, totalTime = updatedTime, date = date))
        } catch (e: Exception) {
            Log.e("ScreenTimeManager", "Error saving app usage: ${e.message}")
        }
    }
}