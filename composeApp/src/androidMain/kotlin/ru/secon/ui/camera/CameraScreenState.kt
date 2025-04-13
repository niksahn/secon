package ru.secon.ui.camera

import ru.secon.core.viewModel.base.State

data class CameraScreenState(
	val makingPhoto: Boolean,
	val uploadedFileId: String?,
) : State()

internal fun init() = CameraScreenState(
	makingPhoto = false,
	uploadedFileId = null,
)