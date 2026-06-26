package com.nova.app.ui.wakeword
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nova.app.engine.wake.NovaWakeWordEngine

class WakeWordActivity : AppCompatActivity() {
    private lateinit var wakeEngine: NovaWakeWordEngine
    private var enrollCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Set Wake Word"
        wakeEngine = NovaWakeWordEngine(this) {}
        wakeEngine.init()
        Toast.makeText(this, "Say your wake word clearly when recording starts", Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}