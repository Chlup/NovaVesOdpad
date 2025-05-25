package com.mugeaters.popelnice.nvpp

import android.app.Application
import android.util.Log
import com.mugeaters.popelnice.nvpp.di.appModule
import com.mugeaters.popelnice.nvpp.util.Logger
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for the NovaVesOdpad app
 */
class NovaVesOdpadApp : Application() {
    
    private val logger: Logger by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        // Direct log to verify LogCat is working
        Log.d("NovaVesOdpad", "🚀 NovaVesOdpadApp.onCreate() - Application starting...")
        
        // Initialize Koin dependency injection
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@NovaVesOdpadApp)
            modules(appModule)
        }
        
        // Use injected logger after Koin initialization
        logger.debug("🚀 NovaVesOdpadApp.onCreate() - Koin initialized successfully")
        logger.debug("🚀 Application setup complete")
    }
}