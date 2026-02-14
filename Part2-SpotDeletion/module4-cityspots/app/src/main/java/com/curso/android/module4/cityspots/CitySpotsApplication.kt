package com.curso.android.module4.cityspots

import android.app.Application
import com.curso.android.module4.cityspots.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * =============================================================================
 * CitySpotsApplication - Clase Application personalizada
 * =============================================================================
 *
 * CONCEPTO: Application Class
 * La clase Application es el punto de entrada de la aplicación Android.
 * Se instancia antes que cualquier Activity, Service, o BroadcastReceiver.
 *
 * USOS COMUNES:
 * 1. Inicializar librerías que requieren Context de aplicación
 * 2. Configurar Dependency Injection (Hilt, Koin, Dagger)
 * 3. Configurar logging, analytics, crash reporting
 * 4. Mantener estado global de la aplicación
 *
 * CONFIGURACIÓN:
 * Para usar una Application personalizada, debe declararse en AndroidManifest.xml:
 *
 * <application
 *     android:name=".CitySpotsApplication"
 *     ... />
 *
 * KOIN DEPENDENCY INJECTION
 * -------------------------
 * Este proyecto usa Koin para DI. Koin se inicializa en onCreate() y
 * proporciona todas las dependencias de la app de forma automática.
 *
 * Beneficios de Koin:
 * - DSL de Kotlin puro (sin anotaciones)
 * - Setup rápido y fácil de entender
 * - Buena integración con Compose via koinViewModel()
 *
 * =============================================================================
 */
class CitySpotsApplication : Application() {

    /**
     * Se llama cuando la aplicación se crea por primera vez
     *
     * Este es el lugar ideal para:
     * - Inicializar componentes que necesitan Application context
     * - Configurar librerías de terceros
     * - NO hacer operaciones largas (bloquea el inicio de la app)
     */
    override fun onCreate() {
        super.onCreate()

        // Guardar referencia global para acceso desde cualquier lugar
        // NOTA: Mantenemos esto por compatibilidad, pero preferimos Koin para DI
        instance = this

        // =====================================================================
        // INICIALIZACIÓN DE KOIN
        // =====================================================================
        // startKoin() configura el framework de DI.
        //
        // COMPONENTES:
        // - androidLogger(): Logger para debug (desactivar en release)
        // - androidContext(): Proporciona Application context a los módulos
        // - modules(): Lista de módulos con definiciones de dependencias
        //
        // ORDEN DE INICIALIZACIÓN:
        // 1. startKoin() crea el contenedor de dependencias
        // 2. Los módulos registran sus definiciones
        // 3. Las dependencias se resuelven cuando se solicitan (lazy)
        // =====================================================================
        startKoin {
            // Logger de Koin para debug (usa Level.NONE en producción)
            androidLogger(Level.DEBUG)

            // Proporciona el Application context a los módulos
            // Esto permite usar androidContext() en las definiciones
            androidContext(this@CitySpotsApplication)

            // Registra los módulos de dependencias
            modules(appModule)
        }
    }

    companion object {
        /**
         * Instancia singleton de la Application
         *
         * NOTA: Este patrón se mantiene por compatibilidad, pero con Koin
         * puedes obtener el context usando androidContext() en los módulos
         * o inject<Application>() donde lo necesites.
         */
        lateinit var instance: CitySpotsApplication
            private set
    }
}
