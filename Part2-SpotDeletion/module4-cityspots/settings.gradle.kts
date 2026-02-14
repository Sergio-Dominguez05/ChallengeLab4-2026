// =============================================================================
// CITY SPOTS - MÓDULO 4: HARDWARE & MAPS
// Settings Configuration
// =============================================================================
// Este archivo configura:
// 1. Los repositorios donde Gradle busca plugins
// 2. Los repositorios donde busca dependencias
// 3. Los módulos incluidos en el proyecto
// =============================================================================

pluginManagement {
    repositories {
        // Google Maven - Android libraries y Google Play Services
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central - Librerías de la comunidad
        mavenCentral()
        // Gradle Plugin Portal - Plugins de Gradle
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS: Falla si un módulo define sus propios repositorios
    // Esto asegura que todos los módulos usen los mismos repositorios
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Nombre del proyecto raíz
rootProject.name = "CitySpots"

// Módulos incluidos en el proyecto
// En este caso, solo tenemos el módulo :app
include(":app")
