package com.nova.app.engine.math
import kotlin.math.*

class Matrix(val rows: Int, val cols: Int) {
    val data: FloatArray = FloatArray(rows * cols)
    operator fun get(r: Int, c: Int): Float = data[r * cols + c]
    operator fun set(r: Int, c: Int, v: Float) { data[r * cols + c] = v }

    companion object {
        fun zeros(rows: Int, cols: Int) = Matrix(rows, cols)
        fun from(rows: Int, cols: Int, values: FloatArray) = Matrix(rows, cols).also { values.copyInto(it.data) }
        fun columnVector(values: FloatArray) = from(values.size, 1, values)
        fun randomHe(rows: Int, cols: Int) = Matrix(rows, cols).apply {
            val limit = sqrt(2f / rows)
            for (i in data.indices) data[i] = ((Math.random() * 2 - 1) * limit).toFloat()
        }
        fun randomXavier(rows: Int, cols: Int) = Matrix(rows, cols).apply {
            val limit = sqrt(6f / (rows + cols))
            for (i in data.indices) data[i] = ((Math.random() * 2 * limit - limit)).toFloat()
        }
        fun ones(rows: Int, cols: Int) = Matrix(rows, cols).apply { data.fill(1f) }
    }

    fun matmul(other: Matrix): Matrix {
        val result = Matrix(rows, other.cols)
        for (r in 0 until rows) for (c in 0 until other.cols) {
            var s = 0f
            for (k in 0 until cols) s += this[r, k] * other[k, c]
            result[r, c] = s
        }
        return result
    }

    operator fun plus(other: Matrix) = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = data[i] + other.data[i] }
    operator fun minus(other: Matrix) = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = data[i] - other.data[i] }
    operator fun times(scalar: Float) = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = data[i] * scalar }
    fun hadamard(other: Matrix) = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = data[i] * other.data[i] }
    fun addBias(bias: Matrix) = Matrix(rows, cols).also { out -> for (r in 0 until rows) for (c in 0 until cols) out[r,c] = this[r,c] + bias[r,0] }
    fun relu() = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = maxOf(0f, data[i]) }
    fun reluDerivative() = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = if (data[i] > 0f) 1f else 0f }
    fun sigmoid() = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = 1f / (1f + exp(-data[i])) }
    fun sigmoidDerivative(): Matrix { val s = sigmoid(); return Matrix(rows, cols).also { for (i in data.indices) it.data[i] = s.data[i] * (1f - s.data[i]) } }
    fun tanh() = Matrix(rows, cols).also { for (i in data.indices) it.data[i] = kotlin.math.tanh(data[i]) }
    fun tanhDerivative() = Matrix(rows, cols).also { for (i in data.indices) { val t = kotlin.math.tanh(data[i]); it.data[i] = 1f - t * t } }
    fun softmax(): Matrix {
        val result = Matrix(rows, cols)
        for (c in 0 until cols) {
            var maxVal = Float.NEGATIVE_INFINITY
            for (r in 0 until rows) maxVal = maxOf(maxVal, this[r, c])
            var sum = 0f
            for (r in 0 until rows) sum += exp(this[r, c] - maxVal)
            for (r in 0 until rows) result[r, c] = exp(this[r, c] - maxVal) / sum
        }
        return result
    }
    fun transpose(): Matrix { val r = Matrix(cols, rows); for (i in 0 until rows) for (j in 0 until cols) r[j,i] = this[i,j]; return r }
    fun flatten() = data.copyOf()
    fun argmax() = data.indices.maxByOrNull { data[it] } ?: 0
    fun copy() = Matrix(rows, cols).also { data.copyInto(it.data) }
}