// =============================================================================
// CITY SPOTS - MÓDULO 4: HARDWARE & MAPS
// Build Configuration (App Module)
// =============================================================================
// Este archivo configura el módulo principal de la aplicación, incluyendo:
// - Plugins necesarios (Android, Kotlin, KSP para Room)
// - Configuración de Android (SDK versions, application ID)
// - Opciones de compilación (Java, Kotlin)
// - Dependencias de la aplicación
// =============================================================================

plugins {
    // Aplica el plugin de Android Application
    alias(libs.plugins.android.application)

    // Aplica el plugin de Kotlin para Android
    alias(libs.plugins.kotlin.android)

    // Aplica el plugin de Compose Compiler
    // NOTA: Requerido desde Kotlin 2.0 para compilar código de Compose
    alias(libs.plugins.kotlin.compose)

    // Aplica KSP para procesamiento de anotaciones de Room
    // KSP es ~2x más rápido que KAPT
    alias(libs.plugins.ksp)
}

import java.util.Properties

// Leer API key desde local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.curso.android.module4.cityspots"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.curso.android.module4.cityspots"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Configuración de vectorDrawables para compatibilidad
        vectorDrawables {
            useSupportLibrary = true
        }

        // Inyectar la API key como manifestPlaceholder
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY") ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configuración de compatibilidad Java
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Configuración de Kotlin
    kotlinOptions {
        jvmTarget = "17"
    }

    // Habilitar Compose
    buildFeatures {
        compose = true
    }

    // Configuración de empaquetado para evitar conflictos con librerías nativas
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // =========================================================================
    // CORE ANDROID
    // =========================================================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // =========================================================================
    // JETPACK COMPOSE
    // =========================================================================
    // BOM (Bill of Materials) - Asegura versiones compatibles de todas las
    // librerías de Compose
    implementation(platform(libs.androidx.compose.bom))

    // Bundle de Compose UI definido en libs.versions.toml
    implementation(libs.bundles.compose)

    // Debugging tools para Compose (solo en debug builds)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // =========================================================================
    // NAVIGATION
    // =========================================================================
    // Navigation Compose para manejar la navegación entre pantallas
    implementation(libs.androidx.navigation.compose)

    // =========================================================================
    // VIEWMODEL
    // =========================================================================
    // ViewModel con soporte para Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // =========================================================================
    // ROOM DATABASE
    // =========================================================================
    // Bundle de Room (runtime + ktx para Flow support)
    implementation(libs.bundles.room)

    // Room Compiler - procesado por KSP para generar implementaciones
    // de DAO y Database
    ksp(libs.androidx.room.compiler)

    // =========================================================================
    // CAMERAX
    // =========================================================================
    // Bundle de CameraX (camera2 + lifecycle + view)
    // - camera2: Implementación del API Camera2
    // - lifecycle: Vinculación automática con el ciclo de vida
    // - view: PreviewView para mostrar la vista previa de la cámara
    implementation(libs.bundles.camerax)

    // =========================================================================
    // GOOGLE MAPS & LOCATION
    // =========================================================================
    // Bundle de Maps (maps-compose + play-services-location + play-services-maps)
    implementation(libs.bundles.maps)

    // =========================================================================
    // PERMISSIONS
    // =========================================================================
    // Accompanist Permissions para manejo declarativo de permisos en Compose
    // NOTA: Esta API está marcada como @ExperimentalPermissionsApi
    implementation(libs.accompanist.permissions)

    // =========================================================================
    // COROUTINES
    // =========================================================================
    // Kotlin Coroutines para Android
    implementation(libs.kotlinx.coroutines.android)

    // =========================================================================
    // IMAGE LOADING
    // =========================================================================
    // Coil para cargar imágenes en Compose
    implementation(libs.coil.compose)

    // =========================================================================
    // DEPENDENCY INJECTION (Koin)
    // =========================================================================
    // Koin es un framework de DI ligero basado en DSL de Kotlin.
    //
    // ¿POR QUÉ KOIN EN LUGAR DE HILT?
    // - Koin: DSL de Kotlin puro, sin generación de código, setup rápido
    // - Hilt: Basado en Dagger, generación de código, más robusto para proyectos grandes
    //
    // Para proyectos educativos/medianos, Koin es más accesible.
    // Para producción enterprise, considera Hilt.
    //
    // BOM para sincronizar versiones
    implementation(platform(libs.koin.bom))
    // Bundle incluye koin-android y koin-compose
    implementation(libs.bundles.koin)

    // =========================================================================
    // TESTING
    // =========================================================================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
