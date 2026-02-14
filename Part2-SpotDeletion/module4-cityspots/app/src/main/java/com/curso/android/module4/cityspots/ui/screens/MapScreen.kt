package com.curso.android.module4.cityspots.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.imageLoader
import coil.request.ImageRequest
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import org.koin.androidx.compose.koinViewModel

/**
 * =============================================================================
 * MapScreen - Pantalla principal con Google Map
 * =============================================================================
 *
 * CONCEPTO: Google Maps Compose Library
 * ---------------------------------------
 * La librería maps-compose provee Composables declarativos para Google Maps:
 * - GoogleMap: El mapa principal
 * - Marker: Marcadores en el mapa
 * - Polygon, Polyline, Circle: Formas geométricas
 * - CameraPositionState: Estado de la cámara del mapa
 *
 * ARQUITECTURA DE LA PANTALLA:
 * ----------------------------
 * MapScreen
 * ├── Scaffold (estructura básica con FAB)
 * │   ├── SpotMap (GoogleMap + Markers)
 * │   ├── SpotInfoCard (Card flotante con detalles)
 * │   └── FloatingActionButton (agregar spot)
 * └── SnackbarHost (mensajes de error)
 *
 * CONCEPTO: State Hoisting
 * ------------------------
 * El estado se "eleva" al ViewModel, y la UI solo observa y renderiza.
 * Esto permite:
 * - Testabilidad del ViewModel sin UI
 * - Separación clara de responsabilidades
 * - Supervivencia a cambios de configuración
 *
 * NOTA IMPORTANTE: Marker vs MarkerInfoWindowContent
 * --------------------------------------------------
 * Originalmente se usaba MarkerInfoWindowContent para mostrar contenido
 * Compose personalizado en el InfoWindow nativo de Google Maps. Sin embargo,
 * esto tiene problemas de timing porque:
 *
 * 1. El InfoWindow se renderiza como un bitmap estático
 * 2. Si el contenido (ej: imagen) no está listo, el bitmap queda vacío
 * 3. No hay forma de actualizar el bitmap una vez renderizado
 *
 * SOLUCIÓN: Usar Marker básico + Card flotante personalizada
 * - Click en marker → muestra Card en la parte inferior
 * - Click en mapa → oculta el Card
 * - Las imágenes se pre-cargan con Coil para evitar delays
 *
 * =============================================================================
 */
@Composable
fun MapScreen(
    onNavigateToCamera: () -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    // =========================================================================
    // OBSERVAR ESTADO DEL VIEWMODEL
    // =========================================================================
    // collectAsState() convierte StateFlow en State de Compose.
    // Compose re-renderiza automáticamente cuando cualquier estado cambia.

    val spots by viewModel.spots.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Estado para Snackbar de errores
    val snackbarHostState = remember { SnackbarHostState() }

    // Context para operaciones con Coil
    val context = LocalContext.current

    // =========================================================================
    // PRE-CARGA DE IMÁGENES
    // =========================================================================
    /**
     * CONCEPTO: Image Preloading con Coil
     *
     * Pre-cargamos las imágenes de todos los spots cuando cambia la lista.
     * Esto asegura que las imágenes estén en el cache de memoria cuando
     * el usuario seleccione un marker, evitando delays visibles.
     *
     * context.imageLoader es el ImageLoader singleton de Coil.
     * enqueue() inicia la carga en background sin bloquear.
     */
    LaunchedEffect(spots) {
        spots.forEach { spot ->
            val request = ImageRequest.Builder(context)
                .data(spot.imageUri.toUri())
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    // =========================================================================
    // EFECTOS SECUNDARIOS (Side Effects)
    // =========================================================================
    /**
     * CONCEPTO: LaunchedEffect
     *
     * Ejecuta código suspendible en respuesta a cambios de estado.
     * - key1 = Unit: Se ejecuta solo una vez al montar el composable
     * - key1 = value: Se re-ejecuta cuando `value` cambia
     *
     * Aquí cargamos la ubicación inicial del usuario al montar la pantalla.
     */
    LaunchedEffect(Unit) {
        viewModel.loadUserLocation()
    }

    // Mostrar errores en Snackbar cuando errorMessage cambia
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // =========================================================================
    // ESTADO DEL MAPA
    // =========================================================================
    /**
     * CONCEPTO: CameraPositionState
     *
     * Controla la posición de la "cámara" del mapa:
     * - position: Centro y zoom actual
     * - animate(): Anima la cámara a una nueva posición
     * - move(): Mueve instantáneamente (sin animación)
     *
     * rememberCameraPositionState sobrevive recomposiciones.
     */
    val cameraPositionState = rememberCameraPositionState {
        // Posición inicial: Ciudad de Guatemala
        position = CameraPosition.fromLatLngZoom(
            LatLng(14.6349, -90.5069),
            12f // Zoom: 1=mundo, 21=calle
        )
    }

    // Animar cámara cuando se obtiene la ubicación del usuario
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
        }
    }

    // =========================================================================
    // UI PRINCIPAL
    // =========================================================================
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamera,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Spot"
                )
            }
        }
    ) { paddingValues ->
        /**
         * CONCEPTO: Estado local para selección
         *
         * selectedSpot es un estado local que determina qué spot
         * está seleccionado actualmente. Cuando no es null, mostramos
         * el SpotInfoCard con los detalles.
         *
         * Este patrón evita los problemas del InfoWindow nativo:
         * - Tenemos control total sobre el renderizado
         * - Podemos mostrar loading states
         * - Las imágenes se cargan correctamente
         */
        var selectedSpot by remember { mutableStateOf<SpotEntity?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mapa con markers
            SpotMap(
                spots = spots,
                userLocation = userLocation,
                cameraPositionState = cameraPositionState,
                onSpotClick = { spot -> selectedSpot = spot },
                onMapClick = { selectedSpot = null }
            )

            // Card flotante con info del spot seleccionado
            selectedSpot?.let { spot ->
                SpotInfoCard(
                    spot = spot,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

            // Indicador de carga centrado
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * =============================================================================
 * SpotMap - Mapa de Google con marcadores
 * =============================================================================
 *
 * CONCEPTO: Marker con onClick
 * ----------------------------
 * Usamos el Marker básico (no MarkerInfoWindowContent) porque:
 *
 * 1. Es más confiable - no tiene problemas de timing
 * 2. Nos permite manejar el click con un callback
 * 3. Podemos mostrar UI personalizada fuera del mapa
 *
 * onClick retorna true para indicar que consumimos el evento.
 * Si retornara false, se mostraría el InfoWindow por defecto.
 *
 * @param spots Lista de spots a mostrar como marcadores
 * @param userLocation Ubicación actual del usuario (para referencia)
 * @param cameraPositionState Estado de la cámara del mapa
 * @param onSpotClick Callback cuando se hace click en un marker
 * @param onMapClick Callback cuando se hace click en el mapa (deseleccionar)
 */
@Composable
private fun SpotMap(
    spots: List<SpotEntity>,
    userLocation: LatLng?,
    cameraPositionState: CameraPositionState,
    onSpotClick: (SpotEntity) -> Unit,
    onMapClick: () -> Unit
) {
    /**
     * CONCEPTO: MapProperties
     *
     * Configura el comportamiento del mapa:
     * - isMyLocationEnabled: Muestra el punto azul de ubicación
     * - mapType: Normal, Satellite, Terrain, Hybrid
     * - isBuildingEnabled: Muestra edificios 3D en zoom alto
     *
     * NOTA: isMyLocationEnabled requiere permiso ACCESS_FINE_LOCATION
     */
    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = true,
            isBuildingEnabled = true
        )
    }

    /**
     * CONCEPTO: MapUiSettings
     *
     * Configura los controles de UI del mapa:
     * - zoomControlsEnabled: Botones +/- para zoom
     * - myLocationButtonEnabled: Botón para centrar en ubicación
     * - compassEnabled: Brújula cuando el mapa está rotado
     * - scrollGesturesEnabled, zoomGesturesEnabled, etc.
     */
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        // Click en cualquier parte del mapa (no en marker)
        onMapClick = { onMapClick() }
    ) {
        /**
         * CONCEPTO: Markers dinámicos
         *
         * Iteramos sobre la lista de spots y creamos un Marker para cada uno.
         * rememberMarkerState mantiene el estado del marker entre recomposiciones.
         *
         * El parámetro `key` es importante para que Compose identifique
         * correctamente cada marker cuando la lista cambia.
         */
        spots.forEach { spot ->
            val markerState = rememberMarkerState(
                key = spot.id.toString(),
                position = LatLng(spot.latitude, spot.longitude)
            )

            Marker(
                state = markerState,
                title = spot.title,
                onClick = {
                    onSpotClick(spot)
                    true // Consumir el click (no mostrar InfoWindow por defecto)
                }
            )
        }
    }
}

/**
 * =============================================================================
 * SpotInfoCard - Card flotante con información del Spot
 * =============================================================================
 *
 * CONCEPTO: Card como alternativa a InfoWindow
 * --------------------------------------------
 * En lugar de usar el InfoWindow nativo de Google Maps (que tiene limitaciones
 * con contenido dinámico), usamos un Card de Material 3 que se superpone
 * sobre el mapa en la parte inferior.
 *
 * VENTAJAS:
 * - Control total sobre el contenido y estilo
 * - Las imágenes se cargan correctamente con estados de loading
 * - Animaciones y transiciones nativas de Compose
 * - No hay problemas de timing con el bitmap rendering
 *
 * CONCEPTO: SubcomposeAsyncImage
 * ------------------------------
 * A diferencia de AsyncImage básico, SubcomposeAsyncImage permite
 * especificar composables diferentes para cada estado:
 * - loading: Mientras la imagen carga
 * - success: Cuando la imagen está lista
 * - error: Si falla la carga
 *
 * Esto es útil para mostrar un CircularProgressIndicator mientras carga.
 *
 * @param spot El spot a mostrar
 * @param modifier Modifier para personalizar posición y padding
 */
@Composable
private fun SpotInfoCard(
    spot: SpotEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del spot con loading state
            SubcomposeAsyncImage(
                model = spot.imageUri.toUri(),
                contentDescription = spot.title,
                modifier = Modifier
                    .size(280.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    // Mostrar spinner mientras carga
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                success = {
                    // Mostrar imagen cuando está lista
                    SubcomposeAsyncImageContent()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Título del spot
            Text(
                text = spot.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Coordenadas formateadas
            Text(
                text = "📍 ${String.format("%.4f", spot.latitude)}, ${String.format("%.4f", spot.longitude)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
