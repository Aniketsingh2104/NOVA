package com.nova.app.commands
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.nova.app.accessibility.NovaAccessibilityService

class CommandExecutor(private val context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val camera = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    data class CommandResult(val success: Boolean, val feedback: String)

    fun execute(command: ParsedCommand, originalText: String): CommandResult = try {
        when (command.type) {
            CommandType.CALL_CONTACT   -> makeCall(command.param)
            CommandType.OPEN_APP       -> openApp(command.param)
            CommandType.GO_HOME        -> accessibility(NovaAccessibilityService.ACTION_HOME)
            CommandType.GO_BACK        -> accessibility(NovaAccessibilityService.ACTION_BACK)
            CommandType.RECENT_APPS    -> accessibility(NovaAccessibilityService.ACTION_RECENTS)
            CommandType.SCROLL_UP      -> accessibility(NovaAccessibilityService.ACTION_SCROLL_UP)
            CommandType.SCROLL_DOWN    -> accessibility(NovaAccessibilityService.ACTION_SCROLL_DOWN)
            CommandType.TAKE_SCREENSHOT-> accessibility(NovaAccessibilityService.ACTION_SCREENSHOT)
            CommandType.LOCK_SCREEN    -> accessibility(NovaAccessibilityService.ACTION_LOCK)
            CommandType.VOLUME_UP      -> vol(AudioManager.ADJUST_RAISE)
            CommandType.VOLUME_DOWN    -> vol(AudioManager.ADJUST_LOWER)
            CommandType.VOLUME_MUTE    -> vol(AudioManager.ADJUST_MUTE)
            CommandType.FLASHLIGHT_ON  -> torch(true)
            CommandType.FLASHLIGHT_OFF -> torch(false)
            CommandType.BRIGHTNESS_UP  -> brightness(true)
            CommandType.BRIGHTNESS_DOWN-> brightness(false)
            CommandType.WIFI_ON        -> wifiSettings()
            CommandType.WIFI_OFF       -> wifiSettings()
            CommandType.BLUETOOTH_ON   -> btSettings()
            CommandType.BLUETOOTH_OFF  -> btSettings()
            CommandType.NOVA_STOP   -> CommandResult(true, "NOVA going to sleep. Say your wake word to activate me again.")
            CommandType.NOVA_HELP   -> CommandResult(true, "You can say: call a contact, open an app, volume up or down, flashlight on or off, take a screenshot, go home, or go back.")
            CommandType.NOVA_STATUS -> CommandResult(true, "Running fully offline. All systems operational.")
            CommandType.UNKNOWN     -> CommandResult(false, "I did not understand that. Try saying help.")
            else                    -> CommandResult(false, "Command not yet implemented.")
        }
    } catch (e: Exception) {
        CommandResult(false, "Something went wrong. Please try again.")
    }

    private fun makeCall(name: String): CommandResult {
        if (name.isBlank()) return CommandResult(false, "Who should I call?")
        return try {
            context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$name")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            CommandResult(true, "Calling $name")
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$name")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            CommandResult(true, "Opening dialer for $name")
        }
    }

    private fun openApp(name: String): CommandResult {
        if (name.isBlank()) return CommandResult(false, "Which app?")
        val pm = context.packageManager
        val match = pm.getInstalledApplications(PackageManager.GET_META_DATA).firstOrNull {
            pm.getApplicationLabel(it).toString().lowercase().contains(name.lowercase())
        }
        return if (match != null) {
            val intent = pm.getLaunchIntentForPackage(match.packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                CommandResult(true, "Opening ${pm.getApplicationLabel(match)}")
            } else CommandResult(false, "Cannot open $name")
        } else CommandResult(false, "App not found: $name")
    }

    private fun vol(dir: Int): CommandResult {
        audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI)
        return CommandResult(true, when (dir) {
            AudioManager.ADJUST_RAISE -> "Volume up"
            AudioManager.ADJUST_LOWER -> "Volume down"
            else -> "Muted"
        })
    }

    private fun torch(on: Boolean): CommandResult = try {
        camera.setTorchMode(camera.cameraIdList[0], on)
        CommandResult(true, if (on) "Flashlight on" else "Flashlight off")
    } catch (e: Exception) { CommandResult(false, "Could not control flashlight") }

    private fun brightness(up: Boolean): CommandResult = try {
        if (Settings.System.canWrite(context)) {
            val cur = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS,
                if (up) minOf(255, cur + 40) else maxOf(10, cur - 40))
            CommandResult(true, if (up) "Brightness increased" else "Brightness decreased")
        } else {
            context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            CommandResult(false, "Please grant write settings permission")
        }
    } catch (e: Exception) { CommandResult(false, "Could not change brightness") }

    private fun wifiSettings(): CommandResult {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        return CommandResult(true, "Opening WiFi settings")
    }

    private fun btSettings(): CommandResult {
        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
        return CommandResult(true, "Opening Bluetooth settings")
    }

    private fun accessibility(action: String): CommandResult {
        val svc = NovaAccessibilityService.instance
        return if (svc != null) {
            svc.performNamedAction(action)
            CommandResult(true, "Done")
        } else {
            CommandResult(false, "Enable Accessibility Service in Settings for this command")
        }
    }
}