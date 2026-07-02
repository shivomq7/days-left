package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.UserSettings
import com.example.data.entity.YearlyGoal
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class DaysLeftUiState(
    val hasBirthDate: Boolean = false,
    val birthDate: LocalDate? = null,
    val lifeExpectancyYears: Int = 66,
    
    // Main counts
    val daysLeft: Long = 0,
    val daysLived: Long = 0,
    val totalExpectedDays: Long = 0,
    val lifeProgress: Float = 0f,
    val currentAge: Int = 0,
    
    // Milestones
    val youthProgress: Float = 0f,
    val youthDaysLeft: Long = 0,
    val isYouthPassed: Boolean = false,
    
    val wisdomProgress: Float = 0f,
    val wisdomDaysLeft: Long = 0,
    val isWisdomPassed: Boolean = false,
    val isWisdomStarted: Boolean = false,
    
    val currentYearOfLifeProgress: Float = 0f,
    val daysToNextBirthday: Long = 0,
    val nextBirthdayAge: Int = 1,
    
    // Custom goals list
    val goals: List<YearlyGoal> = emptyList()
)

class DaysLeftViewModel(private val repository: UserRepository) : ViewModel() {

    val uiState: StateFlow<DaysLeftUiState> = combine(
        repository.userSettings,
        repository.allGoals
    ) { settings, goals ->
        if (settings == null) {
            DaysLeftUiState(hasBirthDate = false, goals = goals)
        } else {
            val birthLocalDate = Instant.ofEpochMilli(settings.birthDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val expectancy = settings.lifeExpectancyYears
            
            calculateMetrics(birthLocalDate, expectancy, goals)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DaysLeftUiState()
    )

    fun saveUserSettings(birthDate: LocalDate, expectancyYears: Int) {
        viewModelScope.launch {
            val millis = birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.saveSettings(UserSettings(birthDateMillis = millis, lifeExpectancyYears = expectancyYears))
        }
    }

    fun addGoal(title: String, year: Int) {
        viewModelScope.launch {
            repository.insertGoal(YearlyGoal(title = title, year = year))
        }
    }

    fun toggleGoal(goal: YearlyGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(isCompleted = !goal.isCompleted))
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    private fun calculateMetrics(
        birthDate: LocalDate,
        expectancyYears: Int,
        goals: List<YearlyGoal>
    ): DaysLeftUiState {
        val today = LocalDate.now()
        
        // Ensure calculations make sense if birthDate is in future
        val birth = if (birthDate.isAfter(today)) today else birthDate
        
        val expirationDate = birth.plusYears(expectancyYears.toLong())
        
        val totalExpectedDays = ChronoUnit.DAYS.between(birth, expirationDate).coerceAtLeast(1)
        val daysLived = ChronoUnit.DAYS.between(birth, today).coerceAtLeast(0)
        val daysLeft = ChronoUnit.DAYS.between(today, expirationDate)
        
        val lifeProgress = (daysLived.toFloat() / totalExpectedDays.toFloat()).coerceIn(0f, 1f)
        val currentAge = ChronoUnit.YEARS.between(birth, today).toInt().coerceAtLeast(0)
        
        // 1. Youth ("Youthfulness") - 45 years
        val youthEndDate = birth.plusYears(45)
        val totalYouthDays = ChronoUnit.DAYS.between(birth, youthEndDate).coerceAtLeast(1)
        val daysLivedInYouth = ChronoUnit.DAYS.between(birth, today).coerceIn(0, totalYouthDays)
        val youthProgress = (daysLivedInYouth.toFloat() / totalYouthDays.toFloat()).coerceIn(0f, 1f)
        val youthDaysLeft = ChronoUnit.DAYS.between(today, youthEndDate).coerceAtLeast(0)
        val isYouthPassed = today.isAfter(youthEndDate) || today.isEqual(youthEndDate)
        
        // 2. Wisdom - 45 years to expectancyYears
        val isWisdomStarted = isYouthPassed
        val wisdomEndDate = expirationDate
        val totalWisdomDays = ChronoUnit.DAYS.between(youthEndDate, wisdomEndDate).coerceAtLeast(1)
        val daysLivedInWisdom = if (isWisdomStarted) {
            ChronoUnit.DAYS.between(youthEndDate, today).coerceIn(0, totalWisdomDays)
        } else {
            0
        }
        val wisdomProgress = (daysLivedInWisdom.toFloat() / totalWisdomDays.toFloat()).coerceIn(0f, 1f)
        val wisdomDaysLeft = if (isWisdomStarted) {
            ChronoUnit.DAYS.between(today, wisdomEndDate).coerceAtLeast(0)
        } else {
            totalWisdomDays
        }
        val isWisdomPassed = today.isAfter(wisdomEndDate)
        
        // 3. Current year of life cycle (e.g. 33rd year progress)
        val lastBirthday = birth.plusYears(currentAge.toLong())
        val nextBirthday = birth.plusYears((currentAge + 1).toLong())
        val totalDaysInYearOfLife = ChronoUnit.DAYS.between(lastBirthday, nextBirthday).coerceAtLeast(1)
        val daysLivedInYearOfLife = ChronoUnit.DAYS.between(lastBirthday, today).coerceIn(0, totalDaysInYearOfLife)
        val currentYearOfLifeProgress = (daysLivedInYearOfLife.toFloat() / totalDaysInYearOfLife.toFloat()).coerceIn(0f, 1f)
        val daysToNextBirthday = ChronoUnit.DAYS.between(today, nextBirthday).coerceAtLeast(0)
        val nextBirthdayAge = currentAge + 1
        
        return DaysLeftUiState(
            hasBirthDate = true,
            birthDate = birth,
            lifeExpectancyYears = expectancyYears,
            daysLeft = daysLeft,
            daysLived = daysLived,
            totalExpectedDays = totalExpectedDays,
            lifeProgress = lifeProgress,
            currentAge = currentAge,
            youthProgress = youthProgress,
            youthDaysLeft = youthDaysLeft,
            isYouthPassed = isYouthPassed,
            wisdomProgress = wisdomProgress,
            wisdomDaysLeft = wisdomDaysLeft,
            isWisdomPassed = isWisdomPassed,
            isWisdomStarted = isWisdomStarted,
            currentYearOfLifeProgress = currentYearOfLifeProgress,
            daysToNextBirthday = daysToNextBirthday,
            nextBirthdayAge = nextBirthdayAge,
            goals = goals
        )
    }
}

class DaysLeftViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DaysLeftViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DaysLeftViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
