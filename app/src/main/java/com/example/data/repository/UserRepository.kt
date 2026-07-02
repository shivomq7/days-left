package com.example.data.repository

import com.example.data.dao.UserSettingsDao
import com.example.data.dao.YearlyGoalDao
import com.example.data.entity.UserSettings
import com.example.data.entity.YearlyGoal
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userSettingsDao: UserSettingsDao,
    private val yearlyGoalDao: YearlyGoalDao
) {
    val userSettings: Flow<UserSettings?> = userSettingsDao.getUserSettings()
    val allGoals: Flow<List<YearlyGoal>> = yearlyGoalDao.getAllGoals()

    suspend fun saveSettings(settings: UserSettings) {
        userSettingsDao.saveUserSettings(settings)
    }

    suspend fun insertGoal(goal: YearlyGoal) {
        yearlyGoalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: YearlyGoal) {
        yearlyGoalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Int) {
        yearlyGoalDao.deleteGoal(id)
    }
}
