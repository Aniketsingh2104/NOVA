package com.nova.app.commands
enum class CommandType {
    CALL_CONTACT, SEND_SMS, OPEN_WHATSAPP,
    OPEN_APP, CLOSE_APP,
    GO_HOME, GO_BACK, RECENT_APPS, SCROLL_UP, SCROLL_DOWN,
    VOLUME_UP, VOLUME_DOWN, VOLUME_MUTE,
    BRIGHTNESS_UP, BRIGHTNESS_DOWN,
    FLASHLIGHT_ON, FLASHLIGHT_OFF,
    WIFI_ON, WIFI_OFF, BLUETOOTH_ON, BLUETOOTH_OFF,
    DND_ON, DND_OFF,
    TAKE_SCREENSHOT, LOCK_SCREEN,
    READ_SCREEN, SUMMARISE_SCREEN, TAP_ELEMENT, TYPE_TEXT,
    AGENT_TASK, REPEAT_LAST, UNDO_LAST,
    NOVA_STOP, NOVA_HELP, NOVA_STATUS,
    UNKNOWN
}
data class ParsedCommand(
    val type: CommandType,
    val params: Map<String, String> = emptyMap()
) {
    fun getParam(key: String, default: String = "") = params[key] ?: default
    val param: String get() = params["param"] ?: params.values.firstOrNull() ?: ""
}