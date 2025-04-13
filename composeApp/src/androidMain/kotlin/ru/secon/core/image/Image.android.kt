package ru.secon.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlin.math.abs

actual class ImageWrapper(private val bitmap: Bitmap) {
    actual fun calculateBlurScore(): Double {
        val (width, height) = bitmap.run { width to height }
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        return calculateLaplacianVariance(
            pixels = pixels,
            width = width,
            height = height
        )
    }

    private fun toGrayscale(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    private fun calculateLaplacianVariance(
        pixels: IntArray,
        width: Int,
        height: Int
    ): Double {
        var sum = 0.0
        var sumSq = 0.0
        val size = width * height

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = toGrayscale(pixels[y * width + x])
                val left = toGrayscale(pixels[y * width + (x - 1)])
                val right = toGrayscale(pixels[y * width + (x + 1)])
                val top = toGrayscale(pixels[(y - 1) * width + x])
                val bottom = toGrayscale(pixels[(y + 1) * width + x])

                val laplacian = (-4 * center + left + right + top + bottom).toDouble()
                sum += laplacian
                sumSq += laplacian * laplacian
            }
        }

        val variance = (sumSq - (sum * sum) / size) / size
        return abs(variance)
    }
}

actual fun ByteArray.toImageWrapper(): ImageWrapper {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, size)
    return ImageWrapper(bitmap)
}