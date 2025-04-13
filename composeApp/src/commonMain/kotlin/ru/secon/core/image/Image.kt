package ru.secon.core.image

expect class ImageWrapper {
    fun calculateBlurScore(): Double
}

expect fun ByteArray.toImageWrapper(): ImageWrapper

data class BlurDetectionResult(
    val isBlurred: Boolean,
    val blurScore: Double,
    val threshold: Double = 50.0 // Эмпирически подобранный порог
) {
    constructor(blurScore: Double) : this(
        isBlurred = blurScore < 50.0,
        blurScore = blurScore
    )
}

suspend fun detectBlur(imageBytes: ByteArray): BlurDetectionResult {
    val image = imageBytes.toImageWrapper()
    val score = image.calculateBlurScore()
    return BlurDetectionResult(score)
}