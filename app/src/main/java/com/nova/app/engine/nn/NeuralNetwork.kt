package com.nova.app.engine.nn
import com.nova.app.engine.math.Matrix
import kotlin.math.*

class NeuralNetwork(val config: NetworkConfig) {
    data class NetworkConfig(val inputSize: Int, val hiddenSizes: List<Int>, val outputSize: Int,
        val activation: String = "relu", val outputActivation: String = "softmax")

    private val weights = mutableListOf<Matrix>()
    private val biases  = mutableListOf<Matrix>()
    private val mW = mutableListOf<Matrix>(); private val vW = mutableListOf<Matrix>()
    private val mB = mutableListOf<Matrix>(); private val vB = mutableListOf<Matrix>()
    private var adamT = 0

    init {
        val sizes = listOf(config.inputSize) + config.hiddenSizes + listOf(config.outputSize)
        for (i in 0 until sizes.size - 1) {
            val w = if (config.activation == "relu") Matrix.randomHe(sizes[i+1], sizes[i]) else Matrix.randomXavier(sizes[i+1], sizes[i])
            val b = Matrix.zeros(sizes[i+1], 1)
            weights.add(w); biases.add(b)
            mW.add(Matrix.zeros(w.rows, w.cols)); vW.add(Matrix.zeros(w.rows, w.cols))
            mB.add(Matrix.zeros(b.rows, b.cols)); vB.add(Matrix.zeros(b.rows, b.cols))
        }
    }

    private fun activate(m: Matrix, fn: String) = when(fn) {
        "relu" -> m.relu(); "sigmoid" -> m.sigmoid(); "tanh" -> m.tanh()
        "softmax" -> m.softmax(); else -> m.relu()
    }
    private fun activateD(m: Matrix, fn: String) = when(fn) {
        "relu" -> m.reluDerivative(); "sigmoid" -> m.sigmoidDerivative(); "tanh" -> m.tanhDerivative()
        else -> Matrix.ones(m.rows, m.cols)
    }

    data class Cache(val pre: List<Matrix>, val act: List<Matrix>)

    fun forward(input: Matrix): Pair<Matrix, Cache> {
        val pre = mutableListOf<Matrix>(); val act = mutableListOf<Matrix>()
        var cur = input
        for (i in weights.indices) {
            val z = weights[i].matmul(cur).addBias(biases[i])
            val a = if (i == weights.size - 1) activate(z, config.outputActivation) else activate(z, config.activation)
            pre.add(z); act.add(a); cur = a
        }
        return Pair(cur, Cache(pre, act))
    }

    fun predict(input: FloatArray): FloatArray { val (o, _) = forward(Matrix.columnVector(input)); return o.flatten() }
    fun predictClass(input: FloatArray) = predict(input).let { p -> p.indices.maxByOrNull { p[it] } ?: 0 }
    fun predictConfidence(input: FloatArray) = predict(input).let { if (it.size > 1) it[1] else it[0] }

    fun trainStep(input: Matrix, target: Matrix, lr: Float = 0.001f): Float {
        val (output, cache) = forward(input)
        val loss = -(0 until target.rows).sumOf { i -> (target[i,0] * ln(maxOf(output[i,0], 1e-10f))).toDouble() }.toFloat()
        var dA = output - target
        adamT++
        val b1 = 0.9f; val b2 = 0.999f; val eps = 1e-8f
        for (i in weights.indices.reversed()) {
            val prevA = if (i == 0) input else cache.act[i-1]
            val dZ = if (i == weights.size-1) dA else dA.hadamard(activateD(cache.pre[i], config.activation))
            val dW = dZ.matmul(prevA.transpose())
            val dB = Matrix(dZ.rows, 1).also { b -> for (r in 0 until dZ.rows) b[r,0] = (0 until dZ.cols).sumOf { c -> dZ[r,c].toDouble() }.toFloat() }
            for (k in mW[i].data.indices) {
                mW[i].data[k] = b1*mW[i].data[k] + (1-b1)*dW.data[k]
                vW[i].data[k] = b2*vW[i].data[k] + (1-b2)*dW.data[k]*dW.data[k]
                weights[i].data[k] -= lr*(mW[i].data[k]/(1-b1.pow(adamT)))/(sqrt(vW[i].data[k]/(1-b2.pow(adamT)))+eps)
            }
            for (k in mB[i].data.indices) {
                mB[i].data[k] = b1*mB[i].data[k] + (1-b1)*dB.data[k]
                vB[i].data[k] = b2*vB[i].data[k] + (1-b2)*dB.data[k]*dB.data[k]
                biases[i].data[k] -= lr*(mB[i].data[k]/(1-b1.pow(adamT)))/(sqrt(vB[i].data[k]/(1-b2.pow(adamT)))+eps)
            }
            dA = weights[i].transpose().matmul(dZ)
        }
        return loss
    }

    fun saveWeights(path: String) {
        java.io.DataOutputStream(java.io.BufferedOutputStream(java.io.FileOutputStream(path))).use { out ->
            out.write("NOVA".toByteArray()); out.writeInt(1); out.writeInt(weights.size * 2)
            for (i in weights.indices) {
                for ((name, mat) in listOf("W$i" to weights[i], "B$i" to biases[i])) {
                    val nb = name.toByteArray(); out.writeInt(nb.size); out.write(nb)
                    out.writeInt(mat.rows); out.writeInt(mat.cols); mat.data.forEach { out.writeFloat(it) }
                }
            }
        }
    }

    fun loadWeights(path: String) {
        java.io.DataInputStream(java.io.BufferedInputStream(java.io.FileInputStream(path))).use { inp ->
            inp.readFully(ByteArray(4)); inp.readInt(); val n = inp.readInt()
            repeat(n) {
                val nameLen = inp.readInt(); val name = ByteArray(nameLen).also { inp.readFully(it) }.toString(Charsets.UTF_8)
                val rows = inp.readInt(); val cols = inp.readInt()
                val data = FloatArray(rows * cols) { inp.readFloat() }
                val idx = name.drop(1).toInt()
                when { name.startsWith("W") && idx < weights.size -> data.copyInto(weights[idx].data)
                       name.startsWith("B") && idx < biases.size  -> data.copyInto(biases[idx].data) }
            }
        }
    }
}