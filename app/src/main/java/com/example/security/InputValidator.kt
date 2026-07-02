package com.example.security

import timber.log.Timber

/**
 * Validates and sanitizes user input to prevent injection attacks and malformed data.
 */
object InputValidator {
    
    /**
     * Validates a date of birth string.
     * Accepts ISO 8601 format (YYYY-MM-DD).
     */
    fun isValidDateOfBirth(dateStr: String): Boolean {
        return try {
            val pattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            pattern.matches(dateStr) && dateStr.length == 10
        } catch (e: Exception) {
            Timber.e(e, "Invalid date format: %s", dateStr)
            false
        }
    }
    
    /**
     * Validates life expectancy value.
     * Must be between 1 and 150 years.
     */
    fun isValidLifeExpectancy(value: Int): Boolean {
        return value in 1..150
    }
    
    /**
     * Sanitizes goal title to prevent injection attacks.
     * Removes special characters and limits length.
     */
    fun sanitizeGoalTitle(title: String): String {
        return title
            .trim()
            .take(MAX_GOAL_LENGTH)
            .replace(Regex("[^a-zA-Z0-9\\s\\-_.,!?'\"()]"), "")
            .ifBlank { "Untitled Goal" }
    }
    
    /**
     * Validates that a goal title is non-empty and reasonable length.
     */
    fun isValidGoalTitle(title: String): Boolean {
        val sanitized = title.trim()
        return sanitized.isNotEmpty() && sanitized.length <= MAX_GOAL_LENGTH
    }
    
    /**
     * Validates a target year for goals.
     * Must be within reasonable bounds from current year.
     */
    fun isValidTargetYear(year: Int): Boolean {
        val currentYear = java.time.Year.now().value
        return year in currentYear..(currentYear + 100)
    }
    
    /**
     * Prevents SQL injection by validating database IDs.
     * Accepts only positive integers.
     */
    fun isValidDatabaseId(id: Int): Boolean {
        return id > 0
    }
    
    companion object {
        private const val MAX_GOAL_LENGTH = 200
    }
}
