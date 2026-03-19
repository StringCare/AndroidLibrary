package com.efraespada.stringobfuscator

import android.app.Application
import dev.vyp.stringcare.library.SC

class StringCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SC.init { applicationContext }
    }
}
