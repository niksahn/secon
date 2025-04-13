package ru.secon.ui.views

import androidx.compose.runtime.Composable
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.darkrockstudios.libraries.mpfilepicker.MPFile

@Composable
actual fun FileChooser(
    showFilePicker: Boolean,
    fileType: List<String>,
    close: () -> Unit,
    loadFile: (MPFile<Any>?) -> Unit
) {
    FilePicker(show = showFilePicker, fileExtensions = fileType) { platformFile ->
        if (platformFile != null) {
            loadFile(platformFile)
        }
        close()
    }
}
