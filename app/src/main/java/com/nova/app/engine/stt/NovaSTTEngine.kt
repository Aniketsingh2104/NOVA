package com.nova.app.engine.stt
import android.content.Context
import android.util.Log

class NovaSTTEngine(private val context: Context, private val onResult: (String) -> Unit) {
    fun init(): Boolean { Log.d(TAG, "STT engine init"); return false }
    fun startListening() { Log.d(TAG, "STT started") }
    fun stopListening() { Log.d(TAG, "STT stopped") }
    fun transcribe(audio: FloatArray): String = ""
    companion object { private const val TAG = "NovaSTTEngine" }
}