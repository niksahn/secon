package ru.secon.ui.tasks.list

import ru.secon.core.monads.Either
import ru.secon.core.monads.Operation
import ru.secon.core.network.NetworkOperation
import ru.secon.core.network.NetworkService
import ru.secon.core.network.toOperation
import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.data.Task
import ru.secon.data.TaskApi

data class TaskState(
    val tasks: NetworkOperation<List<Task>> = Operation.Preparing,
    val reportAvailable: Boolean = false,
    val name: String = "",
    val refresh: Boolean = false
) : State()

sealed class TaskEvent : Event() {
    data class OpenTask(val task: Task) : TaskEvent()
}

class TaskViewModel(
    private val networkService: NetworkService
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
    fun loadReport(){

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