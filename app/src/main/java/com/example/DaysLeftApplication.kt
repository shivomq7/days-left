package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.UserRepository
import com.example.security.SecureKeyManager
import com.example.security.NetworkSecurityManager
import timber.log.Timber

/**
 * Application class responsible for initializing app-wide dependencies.
 * Ensures security components are initialized before the app runs.
 *
 * Created by: 204072937
 */
class DaysLeftApplication : Application() {
    
    // Lazy initialization of security managers
    private val secureKeyManager: SecureKeyManager by lazy {
        SecureKeyManager(this)
    }
    
    // Database initialization
    val database by lazy { 
        AppDatabase.getDatabase(this) 
    }
    
    // Repository initialization with secure configuration
    val repository by lazy { 
        UserRepository(
            database.userSettingsDao(), 
            database.yearlyGoalDao(),
            secureKeyManager = secureKeyManager,
            httpClient = NetworkSecurityManager.createSecureHttpClient()
        ) 
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging (Timber)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Plant a release tree that suppresses debug logs
            Timber.plant(ReleaseTree())
        }
        
        Timber.d("DaysLeftApplication initialized with security hardening")
        Timber.d("Security Features: Encrypted Storage, Input Validation, Certificate Pinning")
    }
    
    /**
     * Custom Timber tree for production builds.
     * Suppresses debug logs and only captures warnings/errors.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?
        ) {
            // Only log warnings and errors in production
            if (priority >= android.util.Log.WARN) {
                super.log(priority, tag, message, t)
            }
        }
    }
}
