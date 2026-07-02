package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.YearlyGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface YearlyGoalDao {
    @Query("SELECT * FROM yearly_goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<YearlyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: YearlyGoal)

    @Update
    suspend fun updateGoal(goal: YearlyGoal)

    @Query("DELETE FROM yearly_goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)
}
