package ru.secon.core.file

import android.app.DownloadManager
import android.content.Context
import androidx.core.net.toUri
import io.ktor.client.statement.HttpResponse

actual class FileDownloader {
    actual suspend fun downloadFile(url: String, outputPath: String) {
//        val downloadManager =
//            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//
//        // Создаем запрос на скачивание
//        val request = DownloadManager.Request(url.toUri())
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(true)
//
//        // Запускаем загрузку
//        val downloadId = downloadManager.enqueue(request)
    }
}

actual suspend fun saveFile(
    response: HttpResponse,
    outputPath: String
) {
}