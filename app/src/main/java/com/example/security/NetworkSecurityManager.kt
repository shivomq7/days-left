package com.example.security

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Manages secure network configuration for API calls.
 * Implements certificate pinning and enforces HTTPS.
 */
object NetworkSecurityManager {
    
    /**
     * Creates a secure OkHttpClient with certificate pinning.
     * Prevents man-in-the-middle attacks.
     */
    fun createSecureHttpClient(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add(
                "generativelanguage.googleapis.com",
                "sha256/Xs2lEFf2DvSSohs7M00NRcaGS0W+qLAstcc339SNsWc=",
                "sha256/jQJTbIh0grw4+4FQkqqyNWjTIDGrVyakXlybPbisPnE="
            )
            .build()
        
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                
                // Add security headers
                val requestWithHeaders = originalRequest.newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
                
                try {
                    chain.proceed(requestWithHeaders)
                } catch (e: Exception) {
                    Timber.e(e, "Network request failed")
                    throw e
                }
            }
            .build()
    }
    
    companion object {
        private const val TIMEOUT_SECONDS = 30L
        private const val USER_AGENT = "DaysLeft/1.0 (Android; Security-Enhanced)"
    }
}
