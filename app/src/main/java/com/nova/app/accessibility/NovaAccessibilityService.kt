package com.nova.app.accessibility
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class NovaAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { super.onDestroy(); instance = null }

    fun performNamedAction(action: String) {
        when (action) {
            ACTION_HOME       -> performGlobalAction(GLOBAL_ACTION_HOME)
            ACTION_BACK       -> performGlobalAction(GLOBAL_ACTION_BACK)
            ACTION_RECENTS    -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            ACTION_SCREENSHOT -> performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            ACTION_LOCK       -> performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            ACTION_SCROLL_UP  -> performScroll(false)
            ACTION_SCROLL_DOWN-> performScroll(true)
        }
    }

    private fun performScroll(down: Boolean) {
        val root = rootInActiveWindow ?: return
        val node = findScrollableNode(root)
        if (node != null) {
            node.performAction(if (down) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
            node.recycle()
        }
        root.recycle()
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val r = findScrollableNode(child)
            if (r != null) return r
            child.recycle()
        }
        return null
    }

    fun tapByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return if (!nodes.isNullOrEmpty()) {
            nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            nodes.forEach { it.recycle() }; root.recycle(); true
        } else { root.recycle(); false }
    }

    fun typeText(text: String) {
        val root = rootInActiveWindow ?: return
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        focused?.let {
            val args = android.os.Bundle().apply {
                putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            it.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            it.recycle()
        }
        root.recycle()
    }

    companion object {
        var instance: NovaAccessibilityService? = null
            private set
        const val ACTION_HOME = "home"
        const val ACTION_BACK = "back"
        const val ACTION_RECENTS = "recents"
        const val ACTION_SCREENSHOT = "screenshot"
        const val ACTION_LOCK = "lock"
        const val ACTION_SCROLL_UP = "scroll_up"
        const val ACTION_SCROLL_DOWN = "scroll_down"
    }
}