package ru.secon.ui.tasks.info

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
actual fun decode(byteArray: ByteArray): ImageBitmap {
    val encodedImageData = Base64.Default.decode(byteArray)
    return Image.makeFromEncoded(encodedImageData).toComposeImageBitmap()
}

actual fun encodeToByteArray(image: String) =
    image.removePrefix("\"\\\"").removeSuffix("\\\"\"").encodeToByteArray()