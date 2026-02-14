package com.curso.android.module4.cityspots.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * =============================================================================
 * PermissionRequest - Componente para manejo de permisos en Runtime
 * =============================================================================
 *
 * CONCEPTO: Permisos en Runtime (Android 6.0+)
 * Desde Android 6.0 (API 23), los permisos "peligrosos" deben solicitarse
 * en tiempo de ejecución, no solo declararse en el Manifest.
 *
 * PERMISOS PELIGROSOS vs NORMALES:
 * - Normales: Se otorgan automáticamente (INTERNET, BLUETOOTH, etc.)
 * - Peligrosos: Requieren aprobación del usuario (CAMERA, LOCATION, etc.)
 *
 * FLUJO DE PERMISOS:
 * 1. Verificar si el permiso está otorgado
 * 2. Si no, verificar si debemos mostrar justificación (shouldShowRationale)
 * 3. Solicitar el permiso
 * 4. Manejar la respuesta (otorgado/denegado)
 *
 * CONCEPTO: Accompanist Permissions
 * Librería de Google que provee APIs declarativas para permisos en Compose:
 * - rememberPermissionState(): Para un solo permiso
 * - rememberMultiplePermissionsState(): Para múltiples permisos
 *
 * NOTA: @ExperimentalPermissionsApi indica que la API puede cambiar
 * en futuras versiones, pero es estable para uso en producción.
 *
 * =============================================================================
 */

/**
 * Lista de permisos requeridos por la aplicación
 *
 * CAMERA: Para capturar fotos de los spots
 * ACCESS_FINE_LOCATION: Para ubicación precisa (GPS)
 * ACCESS_COARSE_LOCATION: Requerido junto con FINE_LOCATION
 */
val requiredPermissions = listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Composable wrapper que maneja el flujo de permisos
 *
 * Este componente:
 * 1. Solicita permisos si no están otorgados
 * 2. Muestra UI apropiada según el estado de permisos
 * 3. Muestra el contenido cuando todos los permisos están otorgados
 *
 * @param content Contenido a mostrar cuando los permisos están otorgados
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequirePermissions(
    content: @Composable () -> Unit
) {
    // Crear estado para múltiples permisos
    // Este estado se recuerda y actualiza automáticamente cuando cambian los permisos
    val permissionsState = rememberMultiplePermissionsState(
        permissions = requiredPermissions
    )

    // Verificar si todos los permisos están otorgados
    if (permissionsState.allPermissionsGranted) {
        // Permisos otorgados: mostrar contenido principal
        content()
    } else {
        // Permisos pendientes: mostrar pantalla de solicitud
        PermissionRequestScreen(permissionsState = permissionsState)
    }
}

/**
 * Pantalla que solicita permisos al usuario
 *
 * Muestra:
 * - Explicación de por qué se necesitan los permisos
 * - Lista de permisos faltantes
 * - Botón para solicitar permisos o abrir configuración
 *
 * @param permissionsState Estado actual de los permisos
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionsState: MultiplePermissionsState
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icono según el contexto
        Icon(
            imageVector = if (permissionsState.shouldShowRationale) {
                Icons.Default.Warning
            } else {
                Icons.Default.CameraAlt
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Título
        Text(
            text = "Permisos Requeridos",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción según el estado
        val description = if (permissionsState.shouldShowRationale) {
            // El usuario ya denegó una vez, mostrar explicación más detallada
            "Has denegado los permisos anteriormente. " +
                "City Spots necesita acceso a la cámara para capturar fotos " +
                "y a la ubicación para guardar las coordenadas de tus spots."
        } else {
            // Primera vez solicitando
            "Para usar City Spots necesitamos acceso a tu cámara " +
                "y ubicación. Esto nos permite:\n\n" +
                "• Capturar fotos de los lugares que visites\n" +
                "• Guardar las coordenadas GPS de cada spot\n" +
                "• Mostrar tus spots en el mapa"
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar permisos faltantes
        PermissionsList(permissionsState)

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de acción
        if (permissionsState.shouldShowRationale) {
            // Si el usuario ya denegó y no hay más intentos,
            // ofrecer ir a configuración
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        // Intentar solicitar de nuevo
                        permissionsState.launchMultiplePermissionRequest()
                    }
                ) {
                    Text("Otorgar Permisos")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        // Abrir configuración de la app
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Abrir Configuración")
                }
            }
        } else {
            // Primera solicitud
            Button(
                onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            ) {
                Text("Otorgar Permisos")
            }
        }
    }
}

/**
 * Lista visual de permisos con su estado
 *
 * Muestra cada permiso faltante con un icono representativo
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionsList(permissionsState: MultiplePermissionsState) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        permissionsState.revokedPermissions.forEach { permission ->
            val (icon, label) = when (permission.permission) {
                Manifest.permission.CAMERA -> Icons.Default.CameraAlt to "Cámara"
                Manifest.permission.ACCESS_FINE_LOCATION -> Icons.Default.LocationOn to "Ubicación Precisa"
                Manifest.permission.ACCESS_COARSE_LOCATION -> Icons.Default.LocationOn to "Ubicación Aproximada"
                else -> Icons.Default.Warning to permission.permission
            }

            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
