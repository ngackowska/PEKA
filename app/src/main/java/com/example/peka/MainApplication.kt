package com.example.peka

import android.app.Application
import org.osmdroid.config.Configuration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = "PekaApp"
    }
}