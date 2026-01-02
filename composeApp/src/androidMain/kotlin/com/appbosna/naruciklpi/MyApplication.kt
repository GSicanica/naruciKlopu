package com.appbosna.naruciklpi

import android.app.Application
import com.appbosna.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 2️⃣ pokrenemo Koin i registriramo taj modul
        initializeKoin(
            config = {
                androidContext(this@MyApplication)
            }
        )
    }
}
