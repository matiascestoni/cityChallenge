package com.test.citychallenge

import android.app.Application
import org.osmdroid.config.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = "github-glenn1wang-myapp"
    }
}