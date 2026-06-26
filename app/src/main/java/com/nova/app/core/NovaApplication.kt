package com.nova.app.core
import android.app.Application
import android.util.Log
import com.nova.app.db.NovaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NovaApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val database: NovaDatabase by lazy { NovaDatabase.getInstance(this) }
    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "NOVA Application started")
    }
    companion object {
        private const val TAG = "NovaApplication"
        lateinit var instance: NovaApplication
            private set
    }
}