package com.curso.android.module4.cityspots.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * =============================================================================
 * LocationUtils - Helper para obtención de ubicación GPS
 * =============================================================================
 *
 * CONCEPTO: FusedLocationProviderClient
 * Es la API de Google Play Services para obtener ubicación. Combina:
 * - GPS (alta precisión, ~10m)
 * - WiFi positioning (~100m)
 * - Cell tower triangulation (~1km)
 *
 * VENTAJAS sobre LocationManager nativo:
 * 1. Optimización de batería automática
 * 2. Mejor precisión combinando múltiples fuentes
 * 3. API más simple y moderna
 * 4. Actualizaciones en segundo plano más eficientes
 *
 * PATRONES UTILIZADOS:
 * - Singleton: Una instancia por aplicación
 * - Coroutines: Operaciones asíncronas con suspend functions
 * - Flow: Stream reactivo de actualizaciones de ubicación
 *
 * =============================================================================
 */
class LocationUtils(context: Context) {

    // Cliente de ubicación de Google Play Services
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la última ubicación conocida de forma one-shot
     *
     * CONCEPTO: suspendCancellableCoroutine
     * Convierte una callback-based API en una suspend function.
     * - resume(): Completa la coroutine con éxito
     * - resumeWithException(): Completa con error
     * - invokeOnCancellation: Limpieza si se cancela
     *
     * @SuppressLint("MissingPermission")
     * Suprimimos el warning porque asumimos que el permiso ya fue verificado
     * antes de llamar a este método. La verificación se hace en el ViewModel/UI.
     *
     * NOTA: getLastLocation puede retornar null si:
     * - El dispositivo nunca ha obtenido ubicación
     * - El usuario desactivó la ubicación recientemente
     * - El cache de ubicación expiró
     *
     * @return Location o null si no hay ubicación disponible
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // location puede ser null
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    /**
     * Obtiene la ubicación actual forzando una actualización fresca
     *
     * DIFERENCIA con getLastLocation():
     * - getLastLocation: Retorna ubicación cacheada (rápido pero posiblemente stale)
     * - getCurrentLocation: Fuerza nueva lectura del GPS (preciso pero más lento)
     *
     * Usamos getCurrentLocation cuando necesitamos la ubicación exacta del momento
     * de captura de una foto.
     *
     * NOTA: Incluye un timeout de 10 segundos para evitar que la coroutine
     * se quede esperando indefinidamente si el GPS no responde.
     *
     * @return Location fresca o null si no se puede obtener (timeout o error)
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        // Timeout de 10 segundos para evitar espera indefinida
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { continuation ->
                // Configurar request de ubicación única
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1000L // Intervalo en milisegundos (no usado para single request)
                )
                    .setWaitForAccurateLocation(false) // No esperar ubicación perfecta
                    .setMinUpdateIntervalMillis(500L)
                    .setMaxUpdates(1) // Solo queremos una actualización
                    .build()

                // Callback para recibir la ubicación
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        continuation.resume(result.lastLocation)
                    }
                }

                // Solicitar actualizaciones de ubicación
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                // Limpiar si la coroutine se cancela
                continuation.invokeOnCancellation {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }
    }

    /**
     * Obtiene actualizaciones continuas de ubicación como Flow
     *
     * CONCEPTO: callbackFlow
     * Crea un Flow que emite valores desde una callback-based API.
     * - trySend(): Emite un valor al Flow
     * - awaitClose(): Se ejecuta cuando el Flow se cancela/cierra
     *
     * CASOS DE USO:
     * - Mostrar ubicación en tiempo real en el mapa
     * - Tracking de movimiento del usuario
     *
     * @param intervalMs Intervalo entre actualizaciones en milisegundos
     * @return Flow de Location que emite nuevas ubicaciones
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 5000L): Flow<Location> = callbackFlow {
        // Configurar request de ubicación continua
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        )
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        // Callback que emite ubicaciones al Flow
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // trySend es non-blocking - si el buffer está lleno, descarta
                    trySend(location)
                }
            }
        }

        // Iniciar actualizaciones
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Limpieza cuando el Flow se cierra
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
