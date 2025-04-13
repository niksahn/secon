package ru.secon.core.file

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import java.io.File
import java.util.Base64
import java.awt.Desktop
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FileDownloader actual constructor() {
    actual suspend fun downloadFile(url: String, outputPath: String) {
        val client = HttpClient()

        downloadWithKtor(client, url, outputPath)
    }
}

actual suspend fun saveFile(response: HttpResponse, outputPath: String) {
    val path = Paths.get(outputPath)
    path.parent?.toFile()?.mkdirs()
    response.bodyAsChannel().copyTo(path.toFile().writeChannel())
}

suspend fun downloadWithKtor(
    client: HttpClient,
    url: String,
    outputPath: String,
): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val response = client.get(url)
        saveFile(response, outputPath)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}


actual class FileSaver  {
    actual fun saveFileFromBase64(
        base64Data: String,
        fileName: String,
        mimeType: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val bytes = Base64.getDecoder().decode(base64Data)
            val fileChooser = JFileChooser().apply {
                selectedFile = File(fileName)
                fileFilter = FileNameExtensionFilter("$mimeType files", mimeType.split("/").last())
            }

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                file.writeBytes(bytes)

                // Открываем файл после сохранения
                Desktop.getDesktop().open(file.parentFile)
                onSuccess()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
    }
}