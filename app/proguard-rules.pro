# ═══════════════════════════════════════════════════════════
# Your Files — ProGuard / R8 Rules
# ═══════════════════════════════════════════════════════════

# --- Keep line numbers for crash reports ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Coil 3 ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- ExoPlayer ---
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# --- WorkManager ---
-keep class androidx.work.** { *; }
-dontwarn androidx.work.impl.**

# --- kotlinx.serialization (if used in future) ---
-dontwarn kotlinx.serialization.**

# --- Keep LocalFile entity for Room ---
-keep class com.yourfiles.manager.data.model.LocalFile { *; }

# --- Navigation Compose safe-args ---
-keepclassmembers class * {
    *** *(...);
}