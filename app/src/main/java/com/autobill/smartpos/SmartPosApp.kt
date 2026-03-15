package com.autobill.smartpos

import android.app.Application
import com.autobill.smartpos.di.AppContainer

// Application class - Entry point for the application
// Initializes the DI container lazily on first access
class SmartPosApp : Application() {
    // Lazy initialization of AppContainer
    // Container is created only when first accessed
    val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }
}
