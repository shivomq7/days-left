# ProGuard rules for code obfuscation and optimization
# This file is proguard configuration for Release builds

# Keep Android framework classes and methods
-keep public class android.** { *; }
-keep public class com.android.** { *; }
-keep public class androidx.** { *; }

# Keep Compose UI framework
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Keep Jetpack libraries
-keep class androidx.lifecycle.** { *; }
-keep class androidx.room.** { *; }
-keep interface androidx.room.** { *; }
-keep class androidx.security.crypto.** { *; }

# Keep Firebase and Gemini SDK classes
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-keep class com.google.ai.** { *; }
-keep interface com.google.ai.** { *; }

# Keep application classes
-keep class com.example.** { *; }
-keep interface com.example.** { *; }

# Keep security manager classes
-keep class com.example.security.** { *; }
-keep interface com.example.security.** { *; }

# Keep data classes with field names
-keep class com.example.data.entity.** { *; }
-keepclassmembers class com.example.data.entity.** {
    *** <fields>;
}

# Keep database DAOs
-keep class com.example.data.dao.** { *; }
-keep interface com.example.data.dao.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Moshi
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }

# Keep Timber logging
-keep class timber.log.** { *; }

# Optimization flags
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-verbose

# Obfuscate
-obfuscationdictionary obfuscation_dictionary.txt
-classobfuscationdictionary obfuscation_dictionary.txt
-packageobfuscationdictionary obfuscation_dictionary.txt

# Remove logging in production
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
