package ru.secon.ui.map

import com.yandex.mapkit.geometry.Point
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

data class MapState(
    val tasks: NetworkOperation<List<Task>> = Operation.Preparing,
    val points: List<Point> = emptyList(),
    val name: String = "",
    val refresh: Boolean = false
) : State()

sealed class TaskEvent : Event()

class MapViewModel(
    private val networkService: NetworkService
) : BaseViewModel<MapState, TaskEvent>(MapState()) {
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
                    points = when (tasksResponse) {
                        is Either.Left -> emptyList()
                        is Either.Right -> tasksResponse.right.mapNotNull {
                            if (it.latitude != null && it.longitude != null) {
                                Point(
                                    it.latitude,
                                    it.longitude
                                )
                            } else {
                                null
                            }
                        }
                    }

                )
            }
        }
    }
}