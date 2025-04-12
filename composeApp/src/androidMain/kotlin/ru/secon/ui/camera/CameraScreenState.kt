package com.niksah.gagarin.screens.camera

import com.niksah.gagarin.utils.base.State

data class CameraScreenState(
	val makingPhoto: Boolean,
	val uploadedFileId: String?,
	val showScanner: Boolean,
	val userId: String
) : State()

enum class CamState {
	VIDEO, PHOTO
}

internal fun init() = CameraScreenState(
	makingPhoto = false,
	uploadedFileId = null,
	showScanner = true,
	userId = ""
)