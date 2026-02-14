package com.curso.android.module4.cityspots.utils

/**
 * =============================================================================
 * CoordinateValidator - Validación de Coordenadas GPS
 * =============================================================================
 *
 * CONCEPTO: Validación de Datos de Entrada
 * ----------------------------------------
 * Siempre valida datos antes de persistirlos, especialmente cuando vienen
 * de hardware (GPS, sensores) que pueden fallar o dar valores incorrectos.
 *
 * RANGOS VÁLIDOS DE COORDENADAS:
 * - Latitud: -90° a +90° (desde el Polo Sur al Polo Norte)
 * - Longitud: -180° a +180° (desde el Meridiano de Greenwich al Antimeridiano)
 *
 * CASOS EDGE:
 * - (0, 0) es el "Null Island" en el Golfo de Guinea - técnicamente válido
 *   pero sospechoso (a menudo indica error de GPS)
 * - Valores NaN o Infinity deben rechazarse
 * - El GPS puede reportar valores fuera de rango en ciertas condiciones
 *
 * BENEFICIO DE VALIDAR:
 * - Evita datos corruptos en la base de datos
 * - Mejora la experiencia del usuario con mensajes claros
 * - Previene crashes en el mapa al renderizar marcadores inválidos
 *
 * =============================================================================
 */
class CoordinateValidator {

    /**
     * Resultado de validación de coordenadas
     */
    sealed class ValidationResult {
        /**
         * Coordenadas válidas
         */
        data object Valid : ValidationResult()

        /**
         * Latitud fuera de rango
         */
        data class InvalidLatitude(val value: Double) : ValidationResult()

        /**
         * Longitud fuera de rango
         */
        data class InvalidLongitude(val value: Double) : ValidationResult()

        /**
         * Valores no numéricos (NaN o Infinity)
         */
        data object NotANumber : ValidationResult()

        /**
         * Posible error de GPS (0,0)
         */
        data object SuspiciousNullIsland : ValidationResult()
    }

    companion object {
        // Rangos válidos según el sistema de coordenadas WGS84
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }

    /**
     * Valida un par de coordenadas GPS
     *
     * @param latitude Latitud a validar
     * @param longitude Longitud a validar
     * @param allowNullIsland Si permitir (0,0) como coordenada válida
     * @return ValidationResult indicando si es válido o el tipo de error
     *
     * EJEMPLO DE USO:
     * ```kotlin
     * when (val result = validator.validate(lat, lng)) {
     *     is ValidationResult.Valid -> { /* Guardar en BD */ }
     *     is ValidationResult.InvalidLatitude -> {
     *         showError("Latitud inválida: ${result.value}")
     *     }
     *     // ...
     * }
     * ```
     */
    fun validate(
        latitude: Double,
        longitude: Double,
        allowNullIsland: Boolean = false
    ): ValidationResult {
        // Verificar valores no numéricos primero
        if (latitude.isNaN() || latitude.isInfinite() ||
            longitude.isNaN() || longitude.isInfinite()) {
            return ValidationResult.NotANumber
        }

        // Verificar rango de latitud
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            return ValidationResult.InvalidLatitude(latitude)
        }

        // Verificar rango de longitud
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            return ValidationResult.InvalidLongitude(longitude)
        }

        // Verificar Null Island (0,0) - sospechoso de error GPS
        if (!allowNullIsland && latitude == 0.0 && longitude == 0.0) {
            return ValidationResult.SuspiciousNullIsland
        }

        return ValidationResult.Valid
    }

    /**
     * Versión simple que retorna boolean
     *
     * @param latitude Latitud a validar
     * @param longitude Longitud a validar
     * @return true si las coordenadas son válidas
     */
    fun isValid(latitude: Double, longitude: Double): Boolean {
        return validate(latitude, longitude) == ValidationResult.Valid
    }

    /**
     * Obtiene mensaje de error legible para el usuario
     *
     * @param result Resultado de validación
     * @return Mensaje descriptivo del error
     */
    fun getErrorMessage(result: ValidationResult): String {
        return when (result) {
            is ValidationResult.Valid -> ""
            is ValidationResult.InvalidLatitude ->
                "Latitud fuera de rango: ${result.value}. Debe estar entre $MIN_LATITUDE y $MAX_LATITUDE"
            is ValidationResult.InvalidLongitude ->
                "Longitud fuera de rango: ${result.value}. Debe estar entre $MIN_LONGITUDE y $MAX_LONGITUDE"
            is ValidationResult.NotANumber ->
                "Las coordenadas contienen valores no numéricos"
            is ValidationResult.SuspiciousNullIsland ->
                "Las coordenadas (0,0) parecen un error de GPS. Intenta de nuevo."
        }
    }
}
