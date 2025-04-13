package ru.secon.ui.tasks.info

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import ru.secon.core.file.FileSaver
import ru.secon.core.image.detectBlur
import ru.secon.core.location.LocationService
import ru.secon.core.monads.Either
import ru.secon.core.monads.Operation
import ru.secon.core.monads.fold
import ru.secon.core.monads.isLeft
import ru.secon.core.network.NetworkOperation
import ru.secon.core.network.NetworkResponse
import ru.secon.core.network.NetworkResponseFailure
import ru.secon.core.network.NetworkService
import ru.secon.core.network.toOperation
import ru.secon.core.utils.InAppNotificationService
import ru.secon.core.utils.SnackbarLayout
import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.data.Control
import ru.secon.data.PhotoApi
import ru.secon.data.ReportApi
import ru.secon.data.Resume
import ru.secon.data.Task
import ru.secon.data.TaskApi
import ru.secon.data.Type
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.blured
import tnsenergoo.composeapp.generated.resources.loaded
import tnsenergoo.composeapp.generated.resources.loading_failed_title
import tnsenergoo.composeapp.generated.resources.not_fill
import kotlin.io.encoding.ExperimentalEncodingApi

data class TaskInfoState(
    val task: Task? = null,
    val name: String = "",
    val firstImage: Operation<ImageBitmap, Unit> = Operation.Preparing,
    val secondImage: Operation<ImageBitmap, Unit> = Operation.Preparing,
    val additional: ActAdditional? = null,
    val actResult: NetworkOperation<Unit>? = null
) : State() {
    sealed class ActAdditional {
        data class New(
            val stop: Boolean = false,
            val communat: Boolean = false,
            val electricity: Electricity = Electricity.LIMITED,
            val deviceParams: String = "",
            val result: ResultTask = ResultTask.SelfLimit,
            val notStartReason: String? = null,
            val clientName: String = "",
            val location: Place = Place.Flat,
            val locationPlace: String? = null
        ) : ActAdditional() {
            enum class ResultTask {
                SelfLimit, InspectorLimit, Resumed, NotStart;

                fun toData() = when (this) {
                    SelfLimit -> 0
                    InspectorLimit -> 1
                    Resumed -> 2
                    NotStart -> 3
                }

                override fun toString(): String = when (this) {
                    SelfLimit -> "Самостоятельное"
                    InspectorLimit -> "ограничение Исполнителем"
                    Resumed -> "возобновление"
                    NotStart -> "не введено"
                }
            }

            enum class Electricity {
                STOP, RESUMED, LIMITED;

                override fun toString(): String = when (this) {
                    STOP -> "приостановлено"
                    RESUMED -> "возобновлено"
                    LIMITED -> "ограничено"
                }

                fun toData() = when (this) {
                    STOP -> 1
                    RESUMED -> 2
                    LIMITED -> 0
                }
            }
        }

        data class Previously(
            val notSelf: Boolean = true,
            val communat: Boolean = false,
            val violation: Boolean = false,
            val location: Place = Place.Flat,
            val deviceParams: String = "",
            val locationPlace: String? = null
        ) : ActAdditional()
    }
}

enum class Place {
    Flat, Stair, Else;

    override fun toString(): String = when (this) {
        Flat -> "в квартире"
        Stair -> "на лестнице"
        Else -> "в другом месте"
    }
}

fun Place.toData() = when (this) {
    Place.Else -> 2
    Place.Flat -> 0
    Place.Stair -> 1
}

sealed class TaskInfoEvent : Event() {
    data class ActFormed(val task: Task) : TaskInfoEvent()
}

class TaskInfoViewModel(
    private val networkService: NetworkService,
    private val inAppNotificationService: InAppNotificationService,
    private val locationService: LocationService,
    private val file: FileSaver
) : BaseViewModel<TaskInfoState, TaskInfoEvent>(
    TaskInfoState()
) {
    fun clear() {
        updateState { it.copy(task = null) }
    }

    fun setTask(task: Task) {
        if (currentState.task != task) {
            updateState {
                it.copy(
                    task = task,
                    additional = when (task.type) {
                        Type.STOP, Type.RESUME -> TaskInfoState.ActAdditional.New()
                        Type.CONTROL -> TaskInfoState.ActAdditional.Previously()
                    },
                    firstImage = task.beforeImage?.takeIf { it.isNotBlank() }
                        ?.let {
                            try {
                                Operation.Success(decode(encodeToByteArray(it)))
                            } catch (e: Exception) {
                                null
                            }
                        }
                        ?: Operation.Failure(Unit),
                    secondImage = task.afterImage?.takeIf { it.isNotBlank() }
                        ?.let {
                            try {
                                Operation.Success(decode(encodeToByteArray(it)))
                            } catch (e: Exception) {
                                null
                            }
                        }
                        ?: Operation.Failure(Unit)
                )
            }
        }
    }

    fun formAct() {
        launchViewModelScope {
            if (!validate()) {
                inAppNotificationService.send(
                    InAppNotificationService.Message(
                        notificationType = SnackbarLayout.NotificationType.Error,
                        titleId = Res.string.not_fill,
                    )
                )
            }
            val task = currentState.task ?: return@launchViewModelScope
            updateState { it.copy(actResult = Operation.Preparing) }
            val rez = when (val data = currentState.additional) {
                is TaskInfoState.ActAdditional.New -> setResume(data)
                is TaskInfoState.ActAdditional.Previously -> setControl(data)
                null -> null
            }
            if (rez?.isLeft() == true) {
                inAppNotificationService.send(
                    InAppNotificationService.Message(
                        notificationType = SnackbarLayout.NotificationType.Error,
                        titleId = Res.string.loading_failed_title,
                    )
                )
                return@launchViewModelScope
            }
            val (longitude: Double, latitude: Double) = locationService.getCoordinates()
            networkService.request(TaskApi.complete(task.id, longitude, latitude))
            updateState { it.copy(actResult = rez?.toOperation()) }
            trySendEvent(TaskInfoEvent.ActFormed(task))
        }
    }

    private fun validate(): Boolean =
        currentState.firstImage is Operation.Success && currentState.secondImage is Operation.Success
                && currentState.additional?.let {
            with(it) {
                when (this) {
                    is TaskInfoState.ActAdditional.New -> {
                        deviceParams.isNotEmpty() &&
                                (notStartReason == null || notStartReason.isNotEmpty()) &&
                                clientName.isNotEmpty() &&
                                (locationPlace == null || locationPlace.isNotEmpty())
                    }

                    is TaskInfoState.ActAdditional.Previously -> {
                        deviceParams.isNotEmpty() &&
                                (locationPlace == null || locationPlace.isNotEmpty())
                    }
                }
            }
        } == true

    private suspend fun setControl(
        additional: TaskInfoState.ActAdditional.Previously
    ): NetworkResponse<Unit> {
        val task = currentState.task ?: return Either.Left(NetworkResponseFailure.Parsing)
        val body = Control(
            requestId = task.id,
            hasViolation = additional.violation,
            hasCommutingDevice = additional.communat,
            meteringDeviceLocationType = additional.location.toData(),
            meteringDeviceLocation = additional.locationPlace.orEmpty(),
            deviceReadings = additional.deviceParams
        )
        return networkService.request(ReportApi.controlAct(body))
    }

    private suspend fun setResume(
        additional: TaskInfoState.ActAdditional.New
    ): NetworkResponse<Unit> {
        val task = currentState.task ?: return Either.Left(NetworkResponseFailure.Parsing)
        val body = Resume(
            requestId = task.id,
            hasCommutingDevice = additional.communat,
            meteringDeviceLocation = additional.location.toData(),
            deviceReadings = additional.deviceParams,
            result = additional.electricity.toData(),
            workMethod = additional.notStartReason.orEmpty(),
            workMethodType = additional.result.toData(),
            clientFullName = additional.clientName
        )
        return networkService.request(ReportApi.resumeAct(body))
    }

    fun loadAct() {
        launchViewModelScope {
            val task = currentState.task ?: return@launchViewModelScope
            updateState { it.copy(actResult = Operation.Preparing) }
            val data = when (task.type) {
                Type.STOP,
                Type.RESUME -> networkService.request(ReportApi.loadResume(task.id))

                Type.CONTROL -> networkService.request(ReportApi.loadControl(task.id))
            }
            data.fold(
                leftTransform = {
                    inAppNotificationService.send(
                        InAppNotificationService.Message(
                            notificationType = SnackbarLayout.NotificationType.Error,
                            titleId = Res.string.loading_failed_title,
                        )
                    )
                },
                rightTransform = {
                    file.saveFileFromBase64(
                        base64Data = it,
                        fileName = "Act",
                        mimeType = "docx",
                        onSuccess = {
                            inAppNotificationService.send(
                                InAppNotificationService.Message(
                                    notificationType = SnackbarLayout.NotificationType.Approve(),
                                    titleId = Res.string.loaded,
                                )
                            )
                        },
                        onError = {
                            inAppNotificationService.send(
                                InAppNotificationService.Message(
                                    notificationType = SnackbarLayout.NotificationType.Error,
                                    titleId = Res.string.loading_failed_title,
                                )
                            )
                        }
                    )
                }

            )
            updateState { it.copy(actResult = null) }
        }
    }

    fun setFirstImage(file: ByteArray) {
        launchViewModelScope {
            val fileArray = file
            val id = currentState.task?.id

            if (!processImage(fileArray) || id == null) return@launchViewModelScope
            updateState { it.copy(firstImage = Operation.Preparing) }
            networkService.request(PhotoApi.setFirstPhoto(fileArray, id))
                .fold(
                    leftTransform = {
                        updateState { it.copy(firstImage = Operation.Failure(Unit)) }
                        inAppNotificationService.send(
                            InAppNotificationService.Message(
                                notificationType = SnackbarLayout.NotificationType.Error,
                                titleId = Res.string.loading_failed_title,
                            )
                        )
                    },
                    rightTransform = {
                        updateState {
                            it.copy(
                                firstImage = Operation.Success(fileArray.decodeToImageBitmap())
                            )
                        }
                    }
                )
        }
    }

    fun setSecondImage(file: MPFile<Any>) {
        launchViewModelScope {
            setSecondImage(file.getFileByteArray())
        }
    }


    fun setFirstImage(file: MPFile<Any>) {
        launchViewModelScope {
            setFirstImage(file.getFileByteArray())
        }
    }

    private suspend fun processImage(file: ByteArray): Boolean {
        val rez = detectBlur(imageBytes = file)
        if (rez.isBlurred) {
            inAppNotificationService.send(
                InAppNotificationService.Message(
                    notificationType = SnackbarLayout.NotificationType.Error,
                    titleId = Res.string.blured,
                )
            )
        }
        return !rez.isBlurred
    }

    fun setSecondImage(file: ByteArray) {
        launchViewModelScope {
            val fileArray = file
            val id = currentState.task?.id
            if (!processImage(fileArray) || id == null) return@launchViewModelScope
            updateState { it.copy(secondImage = Operation.Preparing) }
            networkService.request(PhotoApi.setSecondPhoto(fileArray, id))
                .fold(
                    leftTransform = {
                        updateState { it.copy(secondImage = Operation.Failure(Unit)) }
                        inAppNotificationService.send(
                            InAppNotificationService.Message(
                                notificationType = SnackbarLayout.NotificationType.Error,
                                titleId = Res.string.loading_failed_title,
                            )
                        )
                    },
                    rightTransform = {
                        updateState {
                            it.copy(
                                secondImage = Operation.Success(fileArray.decodeToImageBitmap())
                            )
                        }
                    }
                )
        }
    }

    fun setStop(stop: Boolean) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(stop = stop)

                    is TaskInfoState.ActAdditional.Previously -> it.additional
                    null -> null
                }
            )
        }
    }

    fun setNotSelf(stop: Boolean) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional

                    is TaskInfoState.ActAdditional.Previously -> it.additional
                        .copy(notSelf = stop)

                    null -> null
                }
            )
        }
    }

    fun setDevice(device: String) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(deviceParams = device)

                    is TaskInfoState.ActAdditional.Previously -> it.additional

                    null -> null
                }
            )
        }
    }

    fun setCommunate(stop: Boolean) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(communat = stop)

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional.copy(communat = stop)

                    null -> null
                }
            )
        }
    }

    fun setViolation(stop: Boolean) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional.copy(violation = stop)

                    null -> null
                }
            )
        }
    }

    fun setResult(type: TaskInfoState.ActAdditional.New.ResultTask) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(
                            result = type,
                            notStartReason = ""
                                .takeIf { type == TaskInfoState.ActAdditional.New.ResultTask.NotStart })

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional

                    null -> null
                }
            )
        }
    }

    fun setElectricity(type: TaskInfoState.ActAdditional.New.Electricity) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(electricity = type)

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional

                    null -> null
                }
            )
        }
    }

    fun setLocation(type: Place) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(
                            location = type,
                            locationPlace = "".takeIf { type == Place.Else }
                        )

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional.copy(
                            location = type,
                            locationPlace = "".takeIf { type == Place.Else }
                        )

                    null -> null
                }
            )
        }
    }

    fun setLocation(type: String) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(locationPlace = type)

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional.copy(locationPlace = type)

                    null -> null
                }
            )
        }
    }

    fun setClient(type: String) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(clientName = type)

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional

                    null -> null
                }
            )
        }
    }

    fun setReason(type: String) {
        updateState {
            it.copy(
                additional = when (it.additional) {
                    is TaskInfoState.ActAdditional.New ->
                        it.additional.copy(notStartReason = type)

                    is TaskInfoState.ActAdditional.Previously ->
                        it.additional

                    null -> null
                }
            )
        }
    }
}

expect fun encodeToByteArray(image: String): ByteArray

expect fun decode(byteArray: ByteArray): ImageBitmap
