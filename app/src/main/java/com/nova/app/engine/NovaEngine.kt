package com.nova.app.engine
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nova.app.commands.CommandExecutor
import com.nova.app.commands.CommandType
import com.nova.app.core.NovaApplication
import com.nova.app.db.CommandHistoryEntity
import com.nova.app.engine.nlu.NovaIntentEngine
import com.nova.app.engine.stt.NovaSTTEngine
import com.nova.app.engine.tts.NovaTTSEngine
import com.nova.app.engine.wake.NovaWakeWordEngine
import com.nova.app.ui.home.MainActivity
import kotlinx.coroutines.*

class NovaEngine : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var wakeEngine: NovaWakeWordEngine
    private lateinit var sttEngine: NovaSTTEngine
    private lateinit var intentEngine: NovaIntentEngine
    private lateinit var ttsEngine: NovaTTSEngine
    private lateinit var executor: CommandExecutor
    private var wakeLock: PowerManager.WakeLock? = null
    private var listening = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NovaEngine starting — 100% from scratch")
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("NOVA ready"))
        acquireWakeLock()
        initEngines()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) { ACTION_START -> wakeEngine.start(); ACTION_STOP -> wakeEngine.stop() }
        return START_STICKY
    }

    private fun initEngines() {
        scope.launch {
            ttsEngine = NovaTTSEngine(this@NovaEngine).also { it.init() }
            intentEngine = NovaIntentEngine(this@NovaEngine).also { it.init() }
            executor = CommandExecutor(this@NovaEngine)
            sttEngine = NovaSTTEngine(this@NovaEngine) { text ->
                if (listening && text.isNotBlank()) handleText(text)
            }.also { it.init() }
            wakeEngine = NovaWakeWordEngine(this@NovaEngine) { onWake() }.also { it.init() }
            withContext(Dispatchers.Main) {
                wakeEngine.start()
                updateNotification("NOVA listening — say your wake word")
            }
        }
    }

    private fun onWake() {
        Log.d(TAG, "Wake word detected")
        listening = true
        updateNotification("NOVA: listening...")
        ttsEngine.speak("yes")
        sttEngine.startListening()
        scope.launch { delay(8000); if (listening) { withContext(Dispatchers.Main) { listening = false; sttEngine.stopListening(); updateNotification("NOVA listening — say your wake word") } } }
    }

    private fun handleText(text: String) {
        scope.launch {
            listening = false; sttEngine.stopListening()
            val cmd = intentEngine.parse(text)
            val result = withContext(Dispatchers.Main) { executor.execute(cmd, text) }
            ttsEngine.speak(result.feedback)
            updateNotification("Last: \"${text.take(40)}\"")
            try { NovaApplication.instance.database.commandHistoryDao().insert(
                CommandHistoryEntity(commandText = text, response = result.feedback,
                    timestamp = System.currentTimeMillis(), success = result.success)) } catch (_: Exception) {}
        }
    }

    override fun onDestroy() { super.onDestroy(); wakeEngine.stop(); sttEngine.stopListening(); ttsEngine.shutdown(); wakeLock?.release(); scope.cancel() }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannel(CHANNEL_ID, "NOVA Engine", NotificationManager.IMPORTANCE_LOW)
                .apply { setShowBadge(false) }
                .also { getSystemService(NotificationManager::class.java).createNotificationChannel(it) }
    }
    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("NOVA").setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentIntent(pi).setOngoing(true).setSilent(true).build()
    }
    private fun updateNotification(text: String) = getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(text))
    private fun acquireWakeLock() {
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Nova::WakeLock").also { it.acquire(24*60*60*1000L) }
    }

    companion object {
        private const val TAG = "NovaEngine"; private const val NOTIF_ID = 1002
        private const val CHANNEL_ID = "nova_engine_channel"
        const val ACTION_START = "com.nova.app.ENGINE_START"
        const val ACTION_STOP = "com.nova.app.ENGINE_STOP"
    }
}