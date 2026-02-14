package com.curso.android.module4.cityspots.ui.viewmodel

import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import com.curso.android.module4.cityspots.repository.CreateSpotResult
import com.curso.android.module4.cityspots.repository.SpotRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * =============================================================================
 * MapViewModel - ViewModel para la pantalla principal del mapa
 * =============================================================================
 *
 * CONCEPTO: ViewModel
 * El ViewModel es el componente de la arquitectura MVVM que:
 * 1. Sobrevive a cambios de configuración (rotación, etc.)
 * 2. Contiene la lógica de presentación
 * 3. Expone datos a la UI mediante StateFlow/LiveData
 * 4. No tiene referencia directa a Views/Composables
 *
 * CONCEPTO: ViewModel con DI (Koin)
 * Antes usábamos AndroidViewModel para acceder al Context y crear
 * el Repository internamente. Ahora con Koin:
 * - Usamos ViewModel puro (sin AndroidViewModel)
 * - El Repository se inyecta via constructor
 * - Koin se encarga de resolver las dependencias
 *
 * BENEFICIOS DE ESTE ENFOQUE:
 * 1. **Testabilidad**: Puedes inyectar un mock repository en tests
 * 2. **Desacoplamiento**: ViewModel no conoce cómo se crea el Repository
 * 3. **Pureza**: No hay dependencia de Application/Context
 *
 * CONCEPTO: StateFlow vs LiveData
 * - LiveData: Observable de Lifecycle (tradicional, requiere observers)
 * - StateFlow: Flow que siempre tiene un valor, mejor integración con Compose
 *   StateFlow es preferido en Compose por su naturaleza "composable"
 *
 * =============================================================================
 */
class MapViewModel(
    // Repository inyectado por Koin
    private val repository: SpotRepository
) : ViewModel() {

    // =========================================================================
    // ESTADO DE LA UI
    // =========================================================================

    /**
     * Estado de ubicación del usuario
     *
     * MutableStateFlow es la versión mutable (privada) que podemos actualizar
     * StateFlow es la versión inmutable (pública) que exponemos a la UI
     */
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    /**
     * Estado de carga durante operaciones
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Mensajes de error para mostrar al usuario
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Lista de todos los spots
     *
     * CONCEPTO: stateIn
     * Convierte un Flow en StateFlow, permitiendo:
     * - SharingStarted.WhileSubscribed: El Flow se activa solo cuando hay collectors
     * - 5000ms: Mantiene el Flow activo 5 segundos después del último collector
     *   (evita recrear el Flow en rotaciones rápidas)
     * - emptyList(): Valor inicial mientras el Flow carga
     */
    val spots: StateFlow<List<SpotEntity>> = repository.getAllSpots()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    /**
     * Estado del resultado de captura
     * true = captura exitosa, false = error, null = sin resultado pendiente
     */
    private val _captureResult = MutableStateFlow<Boolean?>(null)
    val captureResult: StateFlow<Boolean?> = _captureResult.asStateFlow()

    // =========================================================================
    // ACCIONES
    // =========================================================================

    /**
     * Carga la ubicación actual del usuario
     *
     * CONCEPTO: viewModelScope
     * Es un CoroutineScope vinculado al ciclo de vida del ViewModel.
     * Las coroutines lanzadas aquí se cancelan automáticamente cuando
     * el ViewModel se destruye, evitando memory leaks.
     */
    fun loadUserLocation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val location = repository.getCurrentLocation()
                location?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error obteniendo ubicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Inicia escucha de actualizaciones de ubicación en tiempo real
     *
     * Útil para mostrar la posición del usuario moviéndose en el mapa
     */
    fun startLocationUpdates() {
        viewModelScope.launch {
            repository.getLocationUpdates()
                .collect { location ->
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                }
        }
    }

    /**
     * Crea un nuevo Spot capturando foto y ubicación
     *
     * @param imageCapture Use case de CameraX configurado
     *
     * MANEJO DE RESULTADOS CON SEALED CLASS
     * -------------------------------------
     * El Repository retorna un CreateSpotResult que puede ser:
     * - Success: Spot creado exitosamente
     * - NoLocation: No se pudo obtener ubicación GPS
     * - InvalidCoordinates: Las coordenadas GPS son inválidas
     *
     * Usar when con sealed class garantiza manejar todos los casos.
     */
    fun createSpot(imageCapture: ImageCapture) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                when (val result = repository.createSpot(imageCapture)) {
                    is CreateSpotResult.Success -> {
                        _captureResult.value = true
                    }

                    is CreateSpotResult.NoLocation -> {
                        _errorMessage.value = "No se pudo obtener la ubicación. Verifica que el GPS esté activado."
                        _captureResult.value = false
                    }

                    is CreateSpotResult.InvalidCoordinates -> {
                        _errorMessage.value = result.message
                        _captureResult.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al capturar: ${e.message}"
                _captureResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Limpia el resultado de captura después de procesarlo
     */
    fun clearCaptureResult() {
        _captureResult.value = null
    }

    /**
     * Limpia el mensaje de error después de mostrarlo
     */
    fun clearError() {
        _errorMessage.value = null
    }


}
