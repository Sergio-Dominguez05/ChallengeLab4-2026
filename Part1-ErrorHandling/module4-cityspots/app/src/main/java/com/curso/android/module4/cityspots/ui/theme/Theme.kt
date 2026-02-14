package com.curso.android.module4.cityspots.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * =============================================================================
 * CitySpotsTheme - Tema de Material Design 3 para la aplicación
 * =============================================================================
 *
 * CONCEPTO: Material Design 3 (Material You)
 * Material 3 es la última versión del sistema de diseño de Google.
 * Características principales:
 * - Dynamic Color: Colores derivados del wallpaper del usuario (Android 12+)
 * - Tonal palettes: Sistema de colores basado en tonos
 * - Updated components: Nuevos estilos para botones, cards, etc.
 *
 * CONCEPTO: ColorScheme
 * Define todos los colores semánticos de la aplicación:
 * - primary, onPrimary: Color principal y texto sobre él
 * - secondary, onSecondary: Color secundario
 * - surface, onSurface: Superficies (cards, sheets) y texto
 * - error, onError: Estados de error
 * - background, onBackground: Fondo de la app
 *
 * =============================================================================
 */

// Colores personalizados para City Spots
private val CitySpotsPrimary = Color(0xFF1976D2)        // Azul Google Maps
private val CitySpotsPrimaryVariant = Color(0xFF0D47A1)
private val CitySpotsSecondary = Color(0xFF4CAF50)      // Verde para markers
private val CitySpotsError = Color(0xFFB00020)

// Esquema de colores para modo claro
private val LightColorScheme = lightColorScheme(
    primary = CitySpotsPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = CitySpotsSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF1B5E20),

    error = CitySpotsError,
    onError = Color.White,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F)
)

// Esquema de colores para modo oscuro
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF388E3C),
    onSecondaryContainer = Color(0xFFC8E6C9),

    error = Color(0xFFCF6679),
    onError = Color.Black,

    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0)
)

/**
 * Tema principal de City Spots
 *
 * @param darkTheme Si usar tema oscuro (por defecto sigue configuración del sistema)
 * @param dynamicColor Si usar colores dinámicos de Material You (Android 12+)
 * @param content Contenido de la aplicación
 */
@Composable
fun CitySpotsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Habilitar Dynamic Color por defecto
    content: @Composable () -> Unit
) {
    /**
     * CONCEPTO: Dynamic Color
     *
     * En Android 12+, el sistema puede extraer colores del wallpaper
     * del usuario y aplicarlos a las apps que lo soporten.
     *
     * dynamicDarkColorScheme/dynamicLightColorScheme generan
     * paletas de colores basadas en el wallpaper actual.
     *
     * Fallback: Si el dispositivo no soporta Dynamic Color,
     * usamos nuestros colores personalizados.
     */
    val colorScheme = when {
        // Dynamic Color disponible en Android 12+ (API 31+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
