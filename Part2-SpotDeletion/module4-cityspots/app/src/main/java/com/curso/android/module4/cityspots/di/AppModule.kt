package com.curso.android.module4.cityspots.di

import com.curso.android.module4.cityspots.data.db.SpotDatabase
import com.curso.android.module4.cityspots.repository.SpotRepository
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel
import com.curso.android.module4.cityspots.utils.CameraUtils
import com.curso.android.module4.cityspots.utils.CoordinateValidator
import com.curso.android.module4.cityspots.utils.LocationUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * =============================================================================
 * APP MODULE - Configuración de Koin
 * =============================================================================
 *
 * INYECCIÓN DE DEPENDENCIAS (DI)
 * ------------------------------
 * DI es un patrón donde los objetos reciben sus dependencias desde el exterior
 * en lugar de crearlas internamente.
 *
 * ANTES (sin DI):
 * ```kotlin
 * class MapViewModel(application: Application) : AndroidViewModel(application) {
 *     private val repository = SpotRepository(application) // ❌ Crea su dependencia
 * }
 * ```
 *
 * DESPUÉS (con DI):
 * ```kotlin
 * class MapViewModel(
 *     private val repository: SpotRepository // ✅ Recibe la dependencia
 * ) : ViewModel()
 * ```
 *
 * BENEFICIOS DE KOIN:
 * 1. **DSL de Kotlin**: Sin anotaciones ni generación de código
 * 2. **Rápido de configurar**: Menos boilerplate que Hilt
 * 3. **Fácil de entender**: Código Kotlin normal
 * 4. **Testing**: Fácil mockear dependencias
 *
 * COMPARACIÓN KOIN vs HILT:
 * | Característica | Koin | Hilt |
 * |----------------|------|------|
 * | Setup | DSL Kotlin | Anotaciones |
 * | Validación | Runtime | Compile-time |
 * | Rendimiento | Ligero | Más optimizado |
 * | Curva aprendizaje | Baja | Media |
 *
 * =============================================================================
 */

/**
 * Módulo principal de dependencias.
 *
 * Define TODAS las dependencias de la aplicación en un solo lugar.
 * En apps más grandes, podrías dividir en múltiples módulos:
 * - dataModule (DB, APIs)
 * - domainModule (Use Cases)
 * - presentationModule (ViewModels)
 */
val appModule = module {

    // =========================================================================
    // CAPA DE DATOS - Database
    // =========================================================================

    /**
     * SpotDatabase como Singleton
     *
     * single { } crea una única instancia compartida en toda la app.
     * Room Database DEBE ser singleton para evitar múltiples conexiones.
     *
     * androidContext() proporciona el Application context de forma segura.
     */
    single {
        SpotDatabase.getInstance(androidContext())
    }

    /**
     * SpotDao
     *
     * Obtenemos el DAO desde la instancia de Database.
     * get() resuelve la dependencia de SpotDatabase automáticamente.
     */
    single {
        get<SpotDatabase>().spotDao()
    }

    // =========================================================================
    // CAPA DE DATOS - Utils
    // =========================================================================

    /**
     * CameraUtils como Singleton
     *
     * Maneja la captura de fotos con CameraX.
     * Singleton porque mantiene configuración interna.
     */
    single {
        CameraUtils(androidContext())
    }

    /**
     * LocationUtils como Singleton
     *
     * Proporciona acceso a servicios de ubicación.
     * Singleton para reutilizar FusedLocationProviderClient.
     */
    single {
        LocationUtils(androidContext())
    }

    /**
     * CoordinateValidator como Singleton
     *
     * Valida rangos de coordenadas GPS.
     * Singleton porque no tiene estado.
     */
    singleOf(::CoordinateValidator)

    // =========================================================================
    // CAPA DE REPOSITORIO
    // =========================================================================

    /**
     * SpotRepository como Singleton
     *
     * Patrón Repository: Abstrae las fuentes de datos.
     * Recibe sus dependencias (DAO, Utils) via Koin.
     *
     * NOTA: El repository ahora recibe sus dependencias en lugar de crearlas.
     * Esto hace el código más testeable y desacoplado.
     */
    single {
        SpotRepository(
            spotDao = get(),
            cameraUtils = get(),
            locationUtils = get(),
            coordinateValidator = get()
        )
    }

    // =========================================================================
    // CAPA DE PRESENTACIÓN - ViewModels
    // =========================================================================

    /**
     * MapViewModel
     *
     * viewModelOf registra el ViewModel con el scope adecuado de Android.
     * Koin automáticamente:
     * 1. Detecta que necesita SpotRepository en el constructor
     * 2. Resuelve esa dependencia del registro
     * 3. Vincula el ViewModel al lifecycle de Compose
     *
     * En Compose, se obtiene con koinViewModel():
     * ```kotlin
     * @Composable
     * fun MapScreen(viewModel: MapViewModel = koinViewModel()) { ... }
     * ```
     */
    viewModelOf(::MapViewModel)
}
