package com.niksah.gagarin.screens.camera

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

@Composable
fun Scanner(
    onResult: (Uri,Context) -> Unit,
    onBack: () -> Unit,
    holdError: ()-> Unit
) {
    val context = LocalContext.current
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
    }
    val scanner = remember { GmsDocumentScanning.getClient(options) }
    val scannerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val gmsResult =
                    GmsDocumentScanningResult.fromActivityResultIntent(result.data)
                gmsResult?.pages?.let { pages ->
                    pages.forEach { page ->
                        onResult(page.imageUri,context)
                    }
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                onBack()
            }
        }
    LaunchedEffect(key1 = Unit) {
        scanner.getStartScanIntent(context as Activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(
                    IntentSenderRequest
                        .Builder(intentSender)
                        .build()
                )
            }.addOnFailureListener {
                holdError()
            }
    }
}