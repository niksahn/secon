package ru.secon.ui.views

import androidx.compose.runtime.Composable
import com.darkrockstudios.libraries.mpfilepicker.MPFile

@Composable
expect fun FileChooser(
    showFilePicker: Boolean,
    fileType: List<String>,
    close: () -> Unit,
    loadFile: (MPFile<Any>?) -> Unit
)