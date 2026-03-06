package com.expofp.minimap

import android.app.Application
import com.expofp.fplan.api.app.ExpoFpPlan
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MiniMapApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ExpoFpPlan.initialize(this)
    }
}
