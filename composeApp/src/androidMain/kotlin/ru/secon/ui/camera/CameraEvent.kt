package ru.secon.ui.camera

import ru.secon.core.viewModel.base.Event


sealed class CameraEvent : Event() {
	data class MakedPhoto(val photo: ByteArray) : CameraEvent()
	data class Failure(val message: String?) : CameraEvent()
}