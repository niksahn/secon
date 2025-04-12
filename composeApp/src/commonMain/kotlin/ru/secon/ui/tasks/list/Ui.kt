package ru.secon.ui.tasks.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.secon.core.monads.Operation
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.data.Task
import ru.secon.ui.views.ErrorState
import ru.secon.ui.views.Loading


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
            Operation.Preparing -> Loading(true)
            is Operation.Success -> Loaded(
                data.value,
                state.value,
                onNew = {},
                onTask = {}
            )
        }
    }

    @Composable
    private fun Loaded(
        data: List<Task>,
        state: TaskState,
        onNew: () -> Unit,
        onTask: (Task) -> Unit
    ) {
        LazyColumn {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = state.name
                    )
                    Button(
                        modifier = Modifier.weight(1f).height(64.dp),
                        content = {
                            Text(
                                text = "Создать"
                            )
                        },
                        onClick = onNew
                    )
                }
            }
            items(items = data) {
                Card(modifier = Modifier.clickable { onTask(it) }) {
                    Row {
                        with(it) {
                            Text("$city $street $apartment, $room")
                        }
                    }
                }
            }
        }
    }
}
