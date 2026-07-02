package com.example.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * Manages secure storage and retrieval of sensitive API keys.
 * Uses EncryptedSharedPreferences for encryption at rest.
 */
class SecureKeyManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Securely stores an API key.
     * @param key The preference key
     * @param value The API key (will be encrypted)
     */
    fun saveApiKey(key: String, value: String) {
        try {
            // Validate key format before storing
            if (!isValidApiKey(value)) {
                Timber.e("Invalid API key format detected - rejected")
                throw SecurityException("Invalid API key format")
            }
            encryptedPreferences.edit().putString(key, value).apply()
            Timber.d("API key stored securely")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save API key securely")
            throw e
        }
    }
    
    /**
     * Securely retrieves an API key.
     * @param key The preference key
     * @param default Default value if key not found
     * @return The decrypted API key
     */
    fun getApiKey(key: String, default: String = ""): String {
        return try {
            encryptedPreferences.getString(key, default) ?: default
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve API key securely")
            default
        }
    }
    
    /**
     * Securely clears a stored API key.
     * @param key The preference key
     */
    fun clearApiKey(key: String) {
        try {
            encryptedPreferences.edit().remove(key).apply()
            Timber.d("API key cleared from secure storage")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear API key")
        }
    }
    
    /**
     * Clears all stored credentials.
     * Use with caution - typically during logout or app reset.
     */
    fun clearAllSecrets() {
        try {
            encryptedPreferences.edit().clear().apply()
            Timber.d("All secrets cleared from secure storage")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear all secrets")
        }
    }
    
    /**
     * Validates API key format to prevent injection attacks.
     * Gemini API keys typically follow a specific format.
     */
    private fun isValidApiKey(apiKey: String): Boolean {
        return apiKey.isNotBlank() && 
               apiKey.length >= MIN_API_KEY_LENGTH && 
               apiKey.matches(VALID_API_KEY_PATTERN)
    }
    
    companion object {
        private const val PREFS_NAME = "com.example.secure_prefs"
        private const val MIN_API_KEY_LENGTH = 20
        
        // API keys should contain alphanumeric characters, hyphens, and underscores
        private val VALID_API_KEY_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
    }
}
