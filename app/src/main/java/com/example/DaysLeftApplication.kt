package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.UserRepository

class DaysLeftApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { UserRepository(database.userSettingsDao(), database.yearlyGoalDao()) }
}
