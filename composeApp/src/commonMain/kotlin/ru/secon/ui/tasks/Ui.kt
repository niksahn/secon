package ru.secon.ui.tasks

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.secon.core.monads.Operation
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.data.Task
import ru.secon.ui.views.ErrorState


data object TasksUi : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<TaskViewModel>()
        val state = viewModel.subscribeScreenState()
        viewModel.subscribeEvents {
            when (it) {
                is TaskEvent.OpenTask -> TODO()
            }
        }
        when (val data = state.value.tasks) {
            is Operation.Failure -> ErrorState(data.value, viewModel::reload)
            Operation.Preparing -> TODO()
            is Operation.Success -> TODO()
        }
    }

    @Composable
    private fun Loaded(
        data: List<Task>
    ) {
        LazyColumn {
            items(items = data){

            }
        }
    }
}