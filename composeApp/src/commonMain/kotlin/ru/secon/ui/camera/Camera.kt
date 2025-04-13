package ru.secon.ui.camera

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ru.secon.ui.tasks.info.Image

expect class CameraUi(image: Image) : Screen {
    val imageVal: Image

    @Composable
    override fun Content()
}
