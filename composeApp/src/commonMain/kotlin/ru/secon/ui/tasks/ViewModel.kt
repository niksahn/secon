package ru.secon.ui.tasks

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
    val name: String = ""
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
            updateState { it.copy(tasks = tasksResponse.toOperation()) }
        }
    }

    fun reload() {}
    fun openTask(id: Task) {
        trySendEvent(TaskEvent.OpenTask(id))
    }
}