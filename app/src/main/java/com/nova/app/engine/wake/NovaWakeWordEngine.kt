package com.nova.app.engine.wake
import android.content.Context
import android.util.Log

class NovaWakeWordEngine(private val context: Context, private val onWakeWordDetected: () -> Unit) {
    fun init(): Boolean { Log.d(TAG, "Wake engine init — train to activate"); return false }
    fun start() { Log.d(TAG, "Wake engine started") }
    fun stop() { Log.d(TAG, "Wake engine stopped") }
    fun recordTrainingSample(isPositive: Boolean, sampleIndex: Int, durationMs: Int = 1500, onDone: (Boolean) -> Unit) { onDone(false) }
    fun trainOnDevice(epochs: Int = 50, lr: Float = 0.001f, onProgress: (Int, Float) -> Unit, onComplete: (Boolean) -> Unit) { onComplete(false) }
    companion object { private const val TAG = "NovaWakeWordEngine" }
}