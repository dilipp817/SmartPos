package com.autobill.smartpos

import android.app.Application
import com.autobill.smartpos.di.AppContainer

class SmartPosApp : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }
}
