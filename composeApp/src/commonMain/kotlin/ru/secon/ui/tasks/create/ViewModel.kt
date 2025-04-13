package ru.secon.ui.tasks.create

import cafe.adriel.voyager.koin.koinScreenModel
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import ru.secon.core.monads.Operation
import ru.secon.core.monads.fold
import ru.secon.core.network.NetworkOperation
import ru.secon.core.network.NetworkService
import ru.secon.core.utils.InAppNotificationService
import ru.secon.core.utils.SnackbarLayout
import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.data.CreateTask
import ru.secon.data.Task
import ru.secon.data.TaskApi
import ru.secon.data.Type
import ru.secon.ui.views.Checkable
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.loading_failed_title
import tnsenergoo.composeapp.generated.resources.not_fill

data class CreateTaskState(
    val creatingTask: NetworkOperation<Unit>? = null,
    val city: String = "",
    val street: String = "",
    val house: String = "",
    val room: String = "",
    val flat: String = "",
    val device: String = "",
    val types: List<Checkable<Type>> = Type.entries.map {
        Checkable(
            text = it.toText(),
            type = it,
            checked = false
        )
    }
) : State()

private fun Type.toText() = when (this) {
    Type.RESUME -> "Возобновление"
    Type.STOP -> "Отключение"
    Type.CONTROL -> "Контроль ранее введенного органичения"
}

sealed class CreateTaskEvent : Event() {
    data class OpenTask(val task: Task) : CreateTaskEvent()
    data object ExelLoaded : CreateTaskEvent()
}

class CreateTaskViewModel(
    private val networkService: NetworkService,
    private val inAppNotificationService: InAppNotificationService
) : BaseViewModel<CreateTaskState, CreateTaskEvent>(CreateTaskState()) {


    fun setCity(value: String) {
        updateState {
            it.copy(
                city = value
            )
        }
    }

    fun setStreet(value: String) {
        updateState {
            it.copy(
                street = value
            )
        }
    }

    fun setRoom(value: String) {
        updateState {
            it.copy(
                room = value
            )
        }
    }

    fun setDevice(value: String) {
        updateState {
            it.copy(
                device = value
            )
        }
    }

    fun setFlat(value: String) {
        updateState {
            it.copy(
                flat = value
            )
        }
    }

    fun setType(type: Type) {
        updateState {
            it.copy(types = it.types.map {
                if (it.type == type) {
                    it.copy(checked = true)
                } else {
                    it.copy(checked = false)
                }
            }
            )
        }
    }

    fun makeTask() {
        launchViewModelScope {
            val valid  = validate()
            if (!valid){
                inAppNotificationService.send(
                    InAppNotificationService.Message(
                        notificationType = SnackbarLayout.NotificationType.Error,
                        titleId = Res.string.not_fill,
                    )
                )
                return@launchViewModelScope
            }
            val task = CreateTask(
                device = screenState.value.device,
                type = screenState.value.types.first { it.checked }.type.toInt(),
                city = screenState.value.city,
                street = screenState.value.street,
                room = screenState.value.room,
                flat = screenState.value.flat,
                house = screenState.value.house
            )
            updateState { it.copy(creatingTask = Operation.Preparing) }
            networkService.request(TaskApi.createTask(task))
                .fold(
                    leftTransform = {
                        inAppNotificationService.send(
                            InAppNotificationService.Message(
                                notificationType = SnackbarLayout.NotificationType.Error,
                                titleId = Res.string.loading_failed_title,
                            )
                        )
                    },
                    rightTransform = {
                        trySendEvent(CreateTaskEvent.ExelLoaded)
                    }
                )
        }
    }

    private fun validate() =
        listOf(
            currentState.city,
            currentState.flat,
            currentState.room,
            currentState.street,
            currentState.device,
        ).all { it.isNotBlank() } && currentState.types.any { it.checked }


    fun loadFromExel(file: MPFile<Any>) {
        launchViewModelScope {
            updateState { it.copy(creatingTask = Operation.Preparing) }
            networkService.request(TaskApi.loadExel(file.getFileByteArray())).fold(
                leftTransform = {
                    inAppNotificationService.send(
                        InAppNotificationService.Message(
                            notificationType = SnackbarLayout.NotificationType.Error,
                            titleId = Res.string.loading_failed_title,
                        )
                    )
                },
                rightTransform = {
                    trySendEvent(CreateTaskEvent.ExelLoaded)
                }
            )
        }
    }

    fun setHouse(type: String) {
        updateState {
            it.copy(
                house = type
            )
        }
    }
}