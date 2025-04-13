package ru.secon.core.file

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import io.ktor.client.statement.HttpResponse
import java.io.File
import java.io.FileOutputStream
import java.util.Base64

actual class FileDownloader {
    var context: Context? = null

    actual suspend fun downloadFile(url: String, outputPath: String) {
        val contextVal = context ?: return
        val downloadManager =
            contextVal.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Создаем запрос на скачивание
        val request = DownloadManager.Request(url.toUri())
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        // Запускаем загрузку
        val downloadId = downloadManager.enqueue(request)
    }
}

actual suspend fun saveFile(
    response: HttpResponse,
    outputPath: String
) {
}

actual class FileSaver {
    var context: Context? = null

    actual fun saveFileFromBase64(
        base64Data: String,
        fileName: String,
        mimeType: String,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val contexrtV = context ?: return
        try {
            val bytes = Base64.getDecoder().decode(base64Data)
            val downloadsDir = contexrtV.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(bytes)
            }

            // Открываем файловый менеджер с новым файлом
            val uri = FileProvider.getUriForFile(
                contexrtV,
                "${contexrtV.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            if (context is Activity) {
                contexrtV.startActivity(intent)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                contexrtV.startActivity(intent)
            }

            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
    }
}
