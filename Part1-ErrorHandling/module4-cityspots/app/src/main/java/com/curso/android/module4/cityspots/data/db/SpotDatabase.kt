package com.curso.android.module4.cityspots.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.curso.android.module4.cityspots.data.dao.SpotDao
import com.curso.android.module4.cityspots.data.entity.SpotEntity

/**
 * =============================================================================
 * SpotDatabase - Base de datos Room
 * =============================================================================
 *
 * CONCEPTO: RoomDatabase
 * La clase Database es el punto de entrada principal para acceder a los datos
 * persistidos. Define:
 * 1. Las entidades (tablas) incluidas en la base de datos
 * 2. La versión del esquema para migraciones
 * 3. Los DAOs disponibles para acceder a los datos
 *
 * ANOTACIONES:
 * @Database:
 * - entities: Array de clases @Entity que forman el esquema
 * - version: Número de versión del esquema (incrementar para migraciones)
 * - exportSchema: Si true, exporta el esquema a JSON para validación
 *
 * PATRÓN SINGLETON:
 * Usamos el patrón Singleton para asegurar que solo exista una instancia
 * de la base de datos en toda la aplicación. Esto:
 * - Previene memory leaks por múltiples conexiones
 * - Asegura consistencia de datos
 * - Es thread-safe gracias a synchronized
 *
 * =============================================================================
 */
@Database(
    entities = [SpotEntity::class],
    version = 1,
    exportSchema = false // En producción, considera exportar para migraciones
)
abstract class SpotDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al SpotDao
     *
     * Room genera automáticamente la implementación de este método
     * que retorna la instancia concreta del DAO
     */
    abstract fun spotDao(): SpotDao

    companion object {
        // Nombre del archivo de base de datos
        private const val DATABASE_NAME = "cityspots_database"

        // Instancia Singleton - @Volatile asegura visibilidad entre threads
        @Volatile
        private var INSTANCE: SpotDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos
         *
         * CONCEPTO: Double-Checked Locking
         * Este patrón optimiza el acceso a recursos compartidos:
         * 1. Primera verificación (sin lock): Evita synchronization innecesario
         * 2. Bloque synchronized: Solo un thread puede crear la instancia
         * 3. Segunda verificación: Otro thread podría haber creado la instancia
         *    mientras esperábamos el lock
         *
         * @param context Context de la aplicación (usar applicationContext)
         * @return Instancia singleton de SpotDatabase
         */
        fun getInstance(context: Context): SpotDatabase {
            // Primera verificación (sin lock)
            return INSTANCE ?: synchronized(this) {
                // Segunda verificación (con lock)
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Construye la instancia de la base de datos
         *
         * CONFIGURACIONES IMPORTANTES:
         * - databaseBuilder: Para bases de datos persistentes en disco
         * - inMemoryDatabaseBuilder: Para testing (datos en RAM)
         *
         * OPCIONES DISPONIBLES:
         * - fallbackToDestructiveMigration(): Elimina datos si no hay migración
         * - addMigrations(): Define migraciones entre versiones
         * - allowMainThreadQueries(): EVITAR - bloquea el UI thread
         *
         * @param context Context de la aplicación
         * @return Nueva instancia de SpotDatabase
         */
        private fun buildDatabase(context: Context): SpotDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SpotDatabase::class.java,
                DATABASE_NAME
            )
            // En desarrollo, destruir y recrear si cambia el esquema
            // En producción, implementar migraciones apropiadas
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
