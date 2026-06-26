package com.nova.app.engine.nlu
import android.content.Context
import com.nova.app.commands.CommandType
import com.nova.app.commands.ParsedCommand

class NovaIntentEngine(private val context: Context) {
    fun init() {}
    fun parse(text: String): ParsedCommand {
        val t = text.lowercase().trim()
        return when {
            t.contains("call") -> ParsedCommand(CommandType.CALL_CONTACT, mapOf("param" to t.substringAfter("call").trim()))
            t.contains("open") -> ParsedCommand(CommandType.OPEN_APP, mapOf("param" to t.substringAfter("open").trim()))
            t.contains("volume up") || t.contains("louder") -> ParsedCommand(CommandType.VOLUME_UP)
            t.contains("volume down") || t.contains("lower") -> ParsedCommand(CommandType.VOLUME_DOWN)
            t.contains("mute") || t.contains("silent") -> ParsedCommand(CommandType.VOLUME_MUTE)
            t.contains("flashlight on") || t.contains("torch on") -> ParsedCommand(CommandType.FLASHLIGHT_ON)
            t.contains("flashlight off") || t.contains("torch off") -> ParsedCommand(CommandType.FLASHLIGHT_OFF)
            t.contains("screenshot") -> ParsedCommand(CommandType.TAKE_SCREENSHOT)
            t.contains("go home") || t == "home" -> ParsedCommand(CommandType.GO_HOME)
            t.contains("go back") || t == "back" -> ParsedCommand(CommandType.GO_BACK)
            t.contains("brightness up") || t.contains("brighter") -> ParsedCommand(CommandType.BRIGHTNESS_UP)
            t.contains("brightness down") || t.contains("dim") -> ParsedCommand(CommandType.BRIGHTNESS_DOWN)
            t.contains("stop") -> ParsedCommand(CommandType.NOVA_STOP)
            t.contains("help") -> ParsedCommand(CommandType.NOVA_HELP)
            else -> ParsedCommand(CommandType.UNKNOWN, mapOf("original" to t))
        }
    }
}