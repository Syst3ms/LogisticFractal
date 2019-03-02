package io.github.syst3ms.logisticfractal

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sqrt

data class Complex(val real: Double = 0.0, val imaginary: Double = 0.0) {
    val distanceSquared = real * real + imaginary * imaginary

    operator fun plus(other: Complex) = Complex(
            this.real + other.real,
            this.imaginary + other.imaginary
    )

    operator fun minus(other: Complex) = Complex(
            this.real - other.real,
            this.imaginary - other.imaginary
    )

    operator fun times(other: Complex) = Complex(
            this.real * other.real - this.imaginary * other.imaginary,
            this.real * other.imaginary + this.imaginary * other.real
    )

    fun abs() = sqrt(distanceSquared)
}

operator fun Number.minus(complex: Complex) = Complex(this.toDouble() - complex.real, complex.imaginary)

private const val MAX_ITER = 2048
private const val ZOOM = 500.0
private const val WIDTH = 2400
private const val HEIGHT = 1350
private const val X_OFFSET = 0
private const val Y_OFFSET = 0

fun main(args: Array<String>) {
    val img = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
    for (y in 0 until HEIGHT) {
        for (x in 0 until WIDTH) {
            val iterArray = MutableList(MAX_ITER, { 0.0 })
            var res = Complex(0.1, 0.0)
            val lambda = Complex((x + X_OFFSET - WIDTH / 2) / ZOOM, (y + Y_OFFSET - HEIGHT / 2) / ZOOM)
            var iter = MAX_ITER
            var smoothColor = exp(-res.abs())
            while (res.distanceSquared < 16.0 && iter > 0) {
                iterArray[MAX_ITER - iter] = res.abs()
                res = lambda * res * (1 - res)
                smoothColor += exp(-res.abs())
                iter--
            }
            if (x == 0)
                println("Row ${y + 1} out of $HEIGHT done")
            img.setRGB(x, y, Color.HSBtoRGB(0.61f + 10 * (smoothColor / MAX_ITER).toFloat(), if (iter > 0) 0.6f else 0.0f, if (iter > 0) 1.0f else periodValue(iterArray.dropLastWhile { it == 0.0 })))
        }
    }
    try {
        var output: File
        var i = 0
        do {
            output = File("output-$i.png")
            i++
        } while (output.exists() && output.isFile)
        ImageIO.write(img, "png", output)
        println("Complete !")
    } catch (e: IOException) {
        println("Couldn't write to file !")
        e.printStackTrace()
    }
}

fun periodValue(list: List<Double>): Float {
    val last = list.last()
    var period = 0
    for (i in 1 until min(30, list.size)) {
        if (last == list[list.size - i - 1]) {
            period = i
            break
        }
    }
    return if (period == 0) 0.0f else 1.0f / (1 + period)
}