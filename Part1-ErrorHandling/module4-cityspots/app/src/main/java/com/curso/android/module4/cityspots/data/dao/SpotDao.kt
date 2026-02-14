package com.curso.android.module4.cityspots.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import kotlinx.coroutines.flow.Flow

/**
 * =============================================================================
 * SpotDao - Data Access Object para Spots
 * =============================================================================
 *
 * CONCEPTO: DAO (Data Access Object) Pattern
 * El DAO es una interfaz que define los métodos para acceder a la base de datos.
 * Room genera automáticamente la implementación en tiempo de compilación.
 *
 * BENEFICIOS DEL PATRÓN DAO:
 * 1. Abstracción: El resto de la app no necesita conocer SQL
 * 2. Testabilidad: Fácil de mockear para unit tests
 * 3. Type-safety: Errores de SQL se detectan en compilación
 * 4. Coroutines: Soporte nativo para operaciones asíncronas
 *
 * TIPOS DE RETORNO:
 * - Flow<List<T>>: Stream reactivo que emite cuando los datos cambian
 * - suspend fun: Función suspendible para operaciones one-shot
 * - LiveData<T>: Observable de Lifecycle (alternativa a Flow)
 *
 * =============================================================================
 */
@Dao
interface SpotDao {

    /**
     * Obtiene todos los spots ordenados por fecha de creación (más reciente primero)
     *
     * CONCEPTO: Flow en Room
     * - Flow es un stream de datos que emite valores de forma asíncrona
     * - Cuando un dato cambia en la BD, Flow emite automáticamente el nuevo valor
     * - Esto permite que la UI se actualice reactivamente
     *
     * @return Flow que emite la lista de spots cada vez que hay cambios
     */
    @Query("SELECT * FROM spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<SpotEntity>>

    /**
     * Obtiene un spot específico por su ID
     *
     * NOTA: suspend indica que esta función debe llamarse desde una coroutine
     * Room ejecutará la query en un hilo de background automáticamente
     *
     * @param id ID del spot a buscar
     * @return SpotEntity o null si no existe
     */
    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getSpotById(id: Long): SpotEntity?

    /**
     * Inserta un nuevo spot en la base de datos
     *
     * CONCEPTO: OnConflictStrategy
     * Define qué hacer si hay conflicto de clave primaria:
     * - ABORT: Abortar la transacción (default)
     * - REPLACE: Reemplazar el registro existente
     * - IGNORE: Ignorar la inserción si ya existe
     *
     * En nuestro caso usamos ABORT porque siempre insertamos nuevos spots
     * con id=0 (auto-generado), así que no debería haber conflictos
     *
     * @param spot SpotEntity a insertar
     * @return ID del nuevo registro insertado
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpot(spot: SpotEntity): Long

    /**
     * Obtiene el conteo total de spots
     *
     * Útil para generar títulos como "Spot #N"
     *
     * @return Número total de spots en la BD
     */
    @Query("SELECT COUNT(*) FROM spots")
    suspend fun getSpotCount(): Int




}
