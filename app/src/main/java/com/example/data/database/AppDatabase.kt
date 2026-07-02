package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.UserSettingsDao
import com.example.data.dao.YearlyGoalDao
import com.example.data.entity.UserSettings
import com.example.data.entity.YearlyGoal

@Database(entities = [UserSettings::class, YearlyGoal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun yearlyGoalDao(): YearlyGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "days_left_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
