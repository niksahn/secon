package ru.secon.ui.tasks.list

import ru.secon.core.file.FileSaver
import ru.secon.core.monads.Either
import ru.secon.core.monads.Operation
import ru.secon.core.monads.fold
import ru.secon.core.network.NetworkOperation
import ru.secon.core.network.NetworkService
import ru.secon.core.network.toOperation
import ru.secon.core.utils.InAppNotificationService
import ru.secon.core.utils.SnackbarLayout
import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.data.Task
import ru.secon.data.TaskApi
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.loaded
import tnsenergoo.composeapp.generated.resources.loading_failed_title

data class TaskState(
    val tasks: NetworkOperation<List<Task>> = Operation.Preparing,
    val reportAvailable: Boolean = false,
    val name: String = "",
    val refresh: Boolean = false,
    val loadingFile: Boolean = false
) : State()

sealed class TaskEvent : Event() {
    data class OpenTask(val task: Task) : TaskEvent()
}

class TaskViewModel(
    private val networkService: NetworkService,
    private val file: FileSaver,
    private val inAppNotificationService: InAppNotificationService,
) : BaseViewModel<TaskState, TaskEvent>(TaskState()) {
    init {
        getTasks()
    }

    private fun getTasks() {
        launchViewModelScope {
            updateState { it.copy(tasks = Operation.Preparing) }
            val tasksResponse = networkService.request(TaskApi.getTasks())
            updateState {
                it.copy(
                    tasks = tasksResponse.toOperation(),
                    reportAvailable = when (tasksResponse) {
                        is Either.Left -> false
                        is Either.Right -> tasksResponse.right.any { it.status == Task.TaskStatus.CLOSED }
                    })
            }
        }
    }

    fun reload() {
        getTasks()
    }

    fun loadReport() {
        launchViewModelScope {
            updateState { it.copy(loadingFile = true) }
            networkService.request(TaskApi.loadReports()).fold(
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
                        fileName = "DayReports",
                        mimeType = "xlsx",
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
            updateState { it.copy(loadingFile = false) }
        }
    }

    fun refresh() {
        launchViewModelScope {
            updateState { it.copy(refresh = true) }
            val tasksResponse = networkService.request(TaskApi.getTasks())
            updateState { it.copy(refresh = false, tasks = tasksResponse.toOperation()) }
        }
    }

    fun openTask(id: Task) {
        trySendEvent(TaskEvent.OpenTask(id))
    }
}