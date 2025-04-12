package com.niksah.gagarin.screens.camera

import com.niksah.gagarin.utils.base.Event

sealed class CameraEvent : Event() {
	object MakedPhoto : CameraEvent()
	data class Failure(val message: String?) : CameraEvent()
}