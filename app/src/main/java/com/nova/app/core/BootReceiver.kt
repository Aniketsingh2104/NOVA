package com.nova.app.core
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nova.app.location.LocationProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d(TAG, "Boot completed — starting NOVA")
        val serviceIntent = Intent(context, NovaListenerService::class.java).apply {
            action = NovaListenerService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        CoroutineScope(Dispatchers.IO).launch {
            try { LocationProfileManager(context).reRegisterAllGeofences() } catch (_: Exception) {}
        }
    }
    companion object { private const val TAG = "BootReceiver" }
}