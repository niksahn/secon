package ru.secon.ui.camera

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ru.secon.ui.tasks.info.Image

actual class CameraUi actual constructor(image: Image) : Screen {
    actual val imageVal: Image
        get() = TODO("Not yet implemented")

    @Composable
    actual override fun Content() {
    }

}