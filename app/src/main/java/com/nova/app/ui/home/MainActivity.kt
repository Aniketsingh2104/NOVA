package com.nova.app.ui.home
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nova.app.accessibility.NovaAccessibilityService
import com.nova.app.core.NovaListenerService
import com.nova.app.databinding.ActivityMainBinding
import com.nova.app.ui.settings.SettingsActivity
import com.nova.app.ui.wakeword.WakeWordActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        checkPermissions()
        startNova()
    }

    override fun onResume() {
        super.onResume()
        val enabled = NovaAccessibilityService.instance != null
        binding.cardAccessibility.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun setupUI() {
        binding.btnToggleNova.setOnClickListener { if (isActive) stopNova() else startNova() }
        binding.btnWakeWord.setOnClickListener { startActivity(Intent(this, WakeWordActivity::class.java)) }
        binding.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.btnEnableAccessibility.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    }

    private fun startNova() {
        val i = Intent(this, NovaListenerService::class.java).apply { action = NovaListenerService.ACTION_START }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
        isActive = true
        binding.tvStatus.text = "NOVA is active"
        binding.btnToggleNova.text = "Pause NOVA"
        binding.indicatorDot.setBackgroundResource(com.nova.app.R.drawable.dot_green)
    }

    private fun stopNova() {
        val i = Intent(this, NovaListenerService::class.java).apply { action = NovaListenerService.ACTION_STOP }
        startService(i)
        isActive = false
        binding.tvStatus.text = "NOVA is paused"
        binding.btnToggleNova.text = "Start NOVA"
        binding.indicatorDot.setBackgroundResource(com.nova.app.R.drawable.dot_gray)
    }

    private fun checkPermissions() {
        val perms = mutableListOf<String>()
        listOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CALL_PHONE,
               android.Manifest.permission.READ_CONTACTS).forEach {
            if (checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED) perms.add(it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED)
                perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
        if (perms.isNotEmpty()) requestPermissions(perms.toTypedArray(), 100)
    }
}