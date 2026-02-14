# =============================================================================
# ProGuard Rules for City Spots
# =============================================================================
# Estas reglas se aplican cuando minifyEnabled = true en build.gradle.kts
# ProGuard/R8 ofusca y optimiza el código para producción.
# =============================================================================

# Mantener anotaciones de Room para que funcione correctamente
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# CameraX
-keep class androidx.camera.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Si usas serialización de datos, descomentar:
# -keepclassmembers class com.curso.android.module4.cityspots.data.entity.** { *; }
