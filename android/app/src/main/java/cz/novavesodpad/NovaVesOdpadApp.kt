package cz.novavesodpad

import android.app.Application
import cz.novavesodpad.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for the NovaVesOdpad app
 */
class NovaVesOdpadApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin dependency injection
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@NovaVesOdpadApp)
            modules(appModule)
        }
    }
}