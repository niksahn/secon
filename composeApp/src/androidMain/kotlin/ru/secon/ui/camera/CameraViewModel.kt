package ru.secon.ui.camera

import android.content.Context
import android.os.Build
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.ImageCaptureResult
import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.data.FileStorageRepository

class
CameraViewModel(
    private val storageRepository: FileStorageRepository,
    private val context: Context
) : BaseViewModel<CameraScreenState, CameraEvent>(init()) {

    fun takePicture(cameraState: CameraState) {
        launchViewModelScope {
            updateState {
                it.copy(makingPhoto = true)
            }
        }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> cameraState.takePicture(
                storageRepository.imageContentValues,
                onResult = ::onImageResult
            )

            else -> cameraState.takePicture(
                storageRepository.getFile("jpg"),
                ::onImageResult
            )
        }
    }

    fun loadFile(file: MPFile<Any>) {
        launchViewModelScope {
            trySendEvent(CameraEvent.MakedPhoto(file.getFileByteArray()))
        }
    }

    private fun onImageResult(imageResult: ImageCaptureResult) {
        launchViewModelScope {
            updateState { it.copy(makingPhoto = false) }
            when (imageResult) {
                is ImageCaptureResult.Error -> trySendEvent(CameraEvent.Failure(imageResult.throwable.message))
                is ImageCaptureResult.Success -> {
                    launchViewModelScope {
                        getFile()?.readBytes()?.let { file ->
                            trySendEvent(CameraEvent.MakedPhoto(file))
                        }
                    }
                }
            }
        }
    }

    fun getFile() = storageRepository.lastFile
}
