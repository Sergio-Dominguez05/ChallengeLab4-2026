package com.curso.android.module4.cityspots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.curso.android.module4.cityspots.ui.components.RequirePermissions
import com.curso.android.module4.cityspots.ui.screens.CameraScreen
import com.curso.android.module4.cityspots.ui.screens.MapScreen
import com.curso.android.module4.cityspots.ui.theme.CitySpotsTheme
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel

/**
 * =============================================================================
 * MainActivity - Activity principal de la aplicación
 * =============================================================================
 *
 * CONCEPTO: Single Activity Architecture
 * En arquitectura moderna de Android, se usa una sola Activity
 * con múltiples "destinos" manejados por Navigation Component.
 *
 * BENEFICIOS:
 * 1. Navegación más fluida (sin recrear Activities)
 * 2. Mejor manejo de estado compartido
 * 3. Transiciones animadas más fáciles
 * 4. Deep links simplificados
 *
 * CONCEPTO: ComponentActivity
 * Clase base que soporta:
 * - Jetpack Compose (setContent)
 * - Activity Result APIs
 * - SavedStateHandle
 * - OnBackPressedDispatcher
 *
 * FLUJO DE LA APP:
 * MainActivity
 * └── CitySpotsTheme
 *     └── RequirePermissions (verifica permisos)
 *         └── NavHost (maneja navegación)
 *             ├── MapScreen (ruta: "map")
 *             └── CameraScreen (ruta: "camera")
 *
 * =============================================================================
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * CONCEPTO: Edge-to-Edge
         * enableEdgeToEdge() permite que la app dibuje detrás
         * de las barras del sistema (status bar, navigation bar).
         * Esto crea una experiencia más inmersiva.
         */
        enableEdgeToEdge()

        /**
         * CONCEPTO: setContent
         * Punto de entrada para Jetpack Compose.
         * Todo el contenido dentro de setContent es @Composable.
         * Reemplaza el tradicional setContentView(R.layout.activity_main)
         */
        setContent {
            CitySpotsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Verificar permisos antes de mostrar la app
                    RequirePermissions {
                        CitySpotsNavigation()
                    }
                }
            }
        }
    }
}

/**
 * Rutas de navegación de la aplicación
 *
 * CONCEPTO: Sealed class/object para rutas
 * Usar sealed class o object para rutas permite:
 * - Type-safety: El compilador verifica rutas válidas
 * - Centralización: Todas las rutas en un lugar
 * - Refactoring seguro: Cambiar una ruta actualiza todos los usos
 */
object NavRoutes {
    const val MAP = "map"
    const val CAMERA = "camera"
}

/**
 * Configuración de navegación de la aplicación
 *
 * CONCEPTO: Navigation Compose
 * Jetpack Navigation para Compose usa:
 * - NavController: Controla la navegación (back, navigate)
 * - NavHost: Contenedor que muestra el destino actual
 * - composable(): Define un destino y su contenido
 *
 * STACK DE NAVEGACIÓN:
 * El NavController mantiene un "back stack" de destinos.
 * navigate() agrega al stack, popBackStack() remueve.
 *
 * Ejemplo de stack:
 * [MapScreen] -> navigate("camera") -> [MapScreen, CameraScreen]
 * -> popBackStack() -> [MapScreen]
 */
@Composable
fun CitySpotsNavigation() {
    // Crear y recordar el NavController
    // rememberNavController() sobrevive recomposiciones
    val navController = rememberNavController()

    /**
     * CONCEPTO: ViewModel compartido
     *
     * Para compartir un ViewModel entre múltiples pantallas,
     * lo creamos en el nivel de navegación y lo pasamos a cada pantalla.
     *
     * Alternativas:
     * - viewModel() sin argumentos: ViewModel por pantalla
     * - hiltViewModel(): Con Hilt DI
     * - viewModel(viewModelStoreOwner): ViewModel compartido
     */
    val sharedViewModel: MapViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAP // Pantalla inicial
    ) {
        /**
         * Destino: Pantalla del Mapa
         *
         * Es la pantalla principal que muestra:
         * - Google Map con spots guardados
         * - FAB para agregar nuevo spot
         * - Ubicación del usuario en tiempo real
         */
        composable(NavRoutes.MAP) {
            MapScreen(
                onNavigateToCamera = {
                    // Navegar a la pantalla de cámara
                    navController.navigate(NavRoutes.CAMERA)
                },
                viewModel = sharedViewModel
            )
        }

        /**
         * Destino: Pantalla de Cámara
         *
         * Muestra la vista previa de la cámara para capturar fotos.
         * Al capturar, obtiene ubicación y guarda en la BD.
         */
        composable(NavRoutes.CAMERA) {
            CameraScreen(
                onNavigateBack = {
                    // Volver a la pantalla anterior (Mapa)
                    navController.popBackStack()
                },
                viewModel = sharedViewModel
            )
        }
    }
}
