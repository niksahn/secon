package ru.secon.core.image

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import kotlin.math.abs

actual class ImageWrapper(private val image: BufferedImage) {
    actual fun calculateBlurScore(): Double {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        image.getRGB(0, 0, width, height, pixels, 0, width)

        return calculateLaplacianVariance(
            pixels = pixels,
            width = width,
            height = height
        )
    }

    private fun toGrayscale(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
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
    val input = ByteArrayInputStream(this)
    val image = ImageIO.read(input)
    return ImageWrapper(image)
}