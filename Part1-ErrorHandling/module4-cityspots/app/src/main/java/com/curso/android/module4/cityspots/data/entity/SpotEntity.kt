package com.curso.android.module4.cityspots.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * =============================================================================
 * SpotEntity - Modelo de datos para Room
 * =============================================================================
 *
 * CONCEPTO: Entity en Room
 * Una Entity representa una tabla en la base de datos SQLite. Cada instancia
 * de la clase corresponde a una fila en la tabla.
 *
 * ANOTACIONES:
 * - @Entity: Marca la clase como una tabla de Room
 *   - tableName: Nombre personalizado de la tabla (por defecto usa el nombre de la clase)
 *
 * - @PrimaryKey: Marca el campo como clave primaria
 *   - autoGenerate: Room genera automáticamente IDs únicos incrementales
 *
 * CAMPOS DE DATOS:
 * - imageUri: URI del archivo de imagen guardado localmente
 *   Ejemplo: "file:///data/user/0/com.curso.android.module4.cityspots/files/spot_1234.jpg"
 *
 * - latitude/longitude: Coordenadas GPS del lugar donde se tomó la foto
 *   Ejemplo: 14.6349, -90.5069 (Ciudad de Guatemala)
 *
 * - title: Título descriptivo del spot
 *   Ejemplo: "Spot #1" (generado automáticamente)
 *
 * - timestamp: Momento en que se creó el spot (milisegundos desde epoch)
 *   Útil para ordenar por fecha de creación
 *
 * =============================================================================
 */
@Entity(tableName = "spots")
data class SpotEntity(
    // Clave primaria auto-generada
    // Room asignará 1, 2, 3... automáticamente
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // URI de la imagen guardada en almacenamiento interno
    // Almacenamos como String porque Room no soporta Uri directamente
    val imageUri: String,

    // Coordenadas GPS donde se tomó la foto
    val latitude: Double,
    val longitude: Double,

    // Título del spot - generado como "Spot #N"
    val title: String,

    // Timestamp de creación para ordenamiento
    // System.currentTimeMillis() por defecto
    val timestamp: Long = System.currentTimeMillis()
)
