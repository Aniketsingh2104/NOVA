package com.nova.app.engine.tts
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class NovaTTSEngine(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false
    fun init() { tts = TextToSpeech(context, this) }
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) { tts?.setLanguage(Locale("en", "IN")); ready = true }
    }
    fun speak(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }
    fun shutdown() { tts?.stop(); tts?.shutdown(); tts = null }
    fun recordPhrase(phrase: String, durationMs: Int = 2000, onDone: (Boolean) -> Unit) { onDone(false) }
    companion object { private const val TAG = "NovaTTSEngine" }
}