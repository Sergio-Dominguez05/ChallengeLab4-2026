package com.curso.android.module4.cityspots.ui.screens

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.curso.android.module4.cityspots.ui.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * =============================================================================
 * CameraScreen - Pantalla de captura de fotos con CameraX
 * =============================================================================
 *
 * CONCEPTO: CameraX
 * CameraX es una librería de Jetpack que simplifica el uso de la cámara:
 * - Lifecycle-aware: Se vincula automáticamente al ciclo de vida
 * - Use Cases: Preview, ImageCapture, VideoCapture, ImageAnalysis
 * - Consistencia: Comportamiento uniforme en diferentes dispositivos
 *
 * CONCEPTO: Interoperabilidad Compose-View (AndroidView)
 * CameraX usa PreviewView (View tradicional) para mostrar la cámara.
 * AndroidView permite integrar Views tradicionales en Compose:
 *
 * AndroidView(
 *     factory = { context -> // Crear la View },
 *     update = { view -> // Actualizar cuando recompone }
 * )
 *
 * ARQUITECTURA DE LA PANTALLA:
 * CameraScreen
 * ├── Scaffold
 * │   ├── TopAppBar (botón atrás, título)
 * │   └── Content
 * │       ├── CameraPreview (AndroidView con PreviewView)
 * │       └── CaptureButton (botón circular de captura)
 *
 * =============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = koinViewModel()
) {
    // =========================================================================
    // CONTEXTO Y LIFECYCLE
    // =========================================================================

    val context = LocalContext.current

    /**
     * CONCEPTO: LocalLifecycleOwner
     * En Compose, obtenemos el LifecycleOwner del árbol de composición.
     * CameraX necesita el LifecycleOwner para:
     * - Iniciar la cámara cuando la Activity está activa
     * - Pausar cuando va a background
     * - Liberar recursos cuando se destruye
     */
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estados del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val captureResult by viewModel.captureResult.collectAsState()

    // Estado para el Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // =========================================================================
    // CONFIGURACIÓN DE CAMERAX
    // =========================================================================

    /**
     * CONCEPTO: ImageCapture Use Case
     *
     * ImageCapture es el "use case" de CameraX para tomar fotos.
     * Configuraciones disponibles:
     * - setCaptureMode: MINIMIZE_LATENCY (rápido) vs MAXIMIZE_QUALITY
     * - setFlashMode: ON, OFF, AUTO
     * - setTargetRotation: Orientación de la imagen
     *
     * remember {} asegura que solo se crea una vez
     */
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    // =========================================================================
    // EFECTOS
    // =========================================================================

    // Manejar resultado de captura
    LaunchedEffect(captureResult) {
        when (captureResult) {
            true -> {
                viewModel.clearCaptureResult()
                onNavigateBack() // Volver al mapa después de captura exitosa
            }
            false -> {
                snackbarHostState.showSnackbar("Error al capturar la foto")
                viewModel.clearCaptureResult()
            }
            null -> { /* Sin resultado pendiente */ }
        }
    }

    // Mostrar errores
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // =========================================================================
    // UI
    // =========================================================================

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Capturar Spot") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Vista previa de la cámara
            CameraPreview(
                imageCapture = imageCapture,
                lifecycleOwner = lifecycleOwner,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con controles
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón de captura
                CaptureButton(
                    onClick = {
                        viewModel.createSpot(imageCapture)
                    },
                    enabled = !isLoading
                )
            }

            // Indicador de carga durante captura
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = "Guardando spot...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable para la vista previa de la cámara
 *
 * CONCEPTO: AndroidView para Interoperabilidad
 *
 * CameraX usa PreviewView, una View tradicional de Android.
 * AndroidView nos permite integrarla en Compose:
 *
 * - factory: Se llama una vez para crear la View
 * - update: Se llama en cada recomposición (puede ser frecuente)
 *
 * IMPORTANTE: La configuración de la cámara debe hacerse en factory
 * o en un LaunchedEffect, no en update (se llamaría muchas veces).
 *
 * @param imageCapture Use case de ImageCapture para vincular
 * @param lifecycleOwner Owner del lifecycle para CameraX
 * @param modifier Modificador de Compose
 */
@Composable
private fun CameraPreview(
    imageCapture: ImageCapture,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Estado para el PreviewView (necesario para configurar en LaunchedEffect)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    /**
     * CONCEPTO: DisposableEffect para limpieza
     *
     * DisposableEffect ejecuta código cuando el composable entra/sale.
     * onDispose se llama cuando el composable se desmonta.
     * Aquí liberamos los recursos de la cámara.
     */
    DisposableEffect(lifecycleOwner) {
        onDispose {
            // Limpiar recursos de CameraX
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Configurar la cámara cuando tenemos el PreviewView
    LaunchedEffect(previewView) {
        previewView?.let { preview ->
            setupCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = preview,
                imageCapture = imageCapture
            )
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Tipo de implementación de la superficie
                // PERFORMANCE: Mejor rendimiento, usa SurfaceView
                // COMPATIBLE: Más compatible, usa TextureView
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }.also {
                previewView = it
            }
        },
        modifier = modifier
    )
}

/**
 * Configura CameraX con Preview e ImageCapture use cases
 *
 * CONCEPTO: ProcessCameraProvider
 *
 * Es el singleton que maneja el acceso a la cámara:
 * 1. getInstance() retorna un Future del provider
 * 2. bindToLifecycle() vincula use cases al lifecycle
 * 3. unbindAll() libera todos los use cases
 *
 * FLUJO:
 * ProcessCameraProvider.getInstance()
 *     .addListener({
 *         provider.unbindAll()  // Liberar uso anterior
 *         provider.bindToLifecycle(
 *             lifecycleOwner,
 *             cameraSelector,
 *             preview,         // Use case: vista previa
 *             imageCapture     // Use case: captura
 *         )
 *     }, executor)
 */
private suspend fun setupCamera(
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    imageCapture: ImageCapture
) {
    // Obtener el CameraProvider de forma suspendible
    val cameraProvider = suspendCoroutine<ProcessCameraProvider> { continuation ->
        ProcessCameraProvider.getInstance(context).addListener({
            continuation.resume(ProcessCameraProvider.getInstance(context).get())
        }, ContextCompat.getMainExecutor(context))
    }

    // Configurar el Preview use case
    val preview = Preview.Builder().build().also {
        // Conectar la superficie del PreviewView con el Preview use case
        it.surfaceProvider = previewView.surfaceProvider
    }

    // Seleccionar cámara trasera
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        // Desvincular cualquier use case anterior
        cameraProvider.unbindAll()

        // Vincular use cases al lifecycle
        // CameraX maneja automáticamente start/stop según el lifecycle
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    } catch (e: Exception) {
        // Error al vincular la cámara (ej: cámara en uso por otra app)
        e.printStackTrace()
    }
}

/**
 * Botón circular para capturar foto
 *
 * Diseño inspirado en apps de cámara estándar:
 * - Círculo blanco grande
 * - Icono de cámara en el centro
 * - Se deshabilita durante la captura
 */
@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(80.dp)
            .background(
                color = if (enabled) Color.White else Color.Gray,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = "Capturar foto",
            tint = if (enabled) Color.Black else Color.DarkGray,
            modifier = Modifier.size(40.dp)
        )
    }
}
