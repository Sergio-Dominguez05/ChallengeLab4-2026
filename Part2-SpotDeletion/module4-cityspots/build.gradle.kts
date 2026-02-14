// =============================================================================
// CITY SPOTS - MÓDULO 4: HARDWARE & MAPS
// Build Configuration (Project Level)
// =============================================================================
// Este archivo configura los plugins disponibles para todo el proyecto.
// Los plugins se declaran aquí pero se aplican en los módulos individuales.
// =============================================================================

plugins {
    // Plugin de Android Application - NO aplicar aquí, solo declarar disponibilidad
    alias(libs.plugins.android.application) apply false

    // Plugin de Kotlin para Android
    alias(libs.plugins.kotlin.android) apply false

    // Plugin de Compose Compiler
    // IMPORTANTE: Desde Kotlin 2.0, el compilador de Compose es un plugin separado
    alias(libs.plugins.kotlin.compose) apply false

    // KSP - Kotlin Symbol Processing
    // Usado por Room para generar código en tiempo de compilación
    // Más eficiente que KAPT (Kotlin Annotation Processing Tool)
    alias(libs.plugins.ksp) apply false
}
