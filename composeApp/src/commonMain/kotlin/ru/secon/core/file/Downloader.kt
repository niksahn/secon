package ru.secon.core.file

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

expect class FileDownloader() {
    suspend fun downloadFile(
        url: String,
        outputPath: String,
    )
}

// Общая часть реализации с Ktor
suspend fun downloadWithKtor(
    client: HttpClient,
    url: String,
    outputPath: String,
    onProgress: (Float) -> Unit = {}
): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val response = client.get(url)

        saveFile(response, outputPath)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

expect suspend fun saveFile(response: HttpResponse, outputPath: String)