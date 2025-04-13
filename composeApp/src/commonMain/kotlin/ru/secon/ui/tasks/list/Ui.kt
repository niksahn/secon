package ru.secon.ui.tasks.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.niksah.gagarin.utils.views.PullToRefreshes
import com.niksah.gagarin.utils.views.rememberPullToRefreshState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import ru.secon.core.monads.Operation
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.data.Task
import ru.secon.ui.tasks.create.CreateTasksUi
import ru.secon.ui.tasks.info.TasksInfoUi
import ru.secon.ui.views.ErrorState
import ru.secon.ui.views.InternetError
import ru.secon.ui.views.Loading
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.calendar

data object TasksUi : Screen {
    private fun readResolve(): Any = TasksUi

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<TaskViewModel>()
        val state = viewModel.subscribeScreenState()
        viewModel.subscribeEvents {
            when (it) {
                is TaskEvent.OpenTask -> TODO()
            }
        }
        Column(modifier = Modifier.padding(top = 32.dp).padding(horizontal = 16.dp).fillMaxSize()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.height(64.dp)
                        .let {
                            it.then(
                                if (!state.value.reportAvailable) {
                                    it.fillMaxSize()
                                } else {
                                    it
                                }
                            )
                        },
                    content = {
                        Text(
                            text = "Создать задачу"
                        )
                    },
                    onClick = {
                        navigator.push(CreateTasksUi)
                    }
                )
                if (state.value.reportAvailable) {
                    Button(
                        modifier = Modifier.height(64.dp),
                        content = {
                            if (state.value.loadingFile) {
                                Loading(true)
                            } else {
                                Text(text = "Загрузить дневной отчет")
                            }
                        },
                        onClick = { viewModel.loadReport() }
                    )
                }
            }

            when (val data = state.value.tasks) {
                is Operation.Failure -> ErrorState(data.value, viewModel::reload)
                Operation.Preparing -> Loading(true)
                is Operation.Success ->
                    if (data.value.isNotEmpty()) {
                        Loaded(
                            data.value,
                            onTask = {
                                navigator.push(TasksInfoUi(it))
                            },
                            state = state.value,
                            onRefresh = viewModel::refresh
                        )
                    } else {
                        Empty(viewModel::reload)
                    }
            }
        }
    }
}

@Composable
private fun Loaded(
    data: List<Task>,
    onTask: (Task) -> Unit,
    state: TaskState,
    onRefresh: () -> Unit
) {
    PullToRefreshes.Primary(
        state = rememberPullToRefreshState(state.refresh),
        onRefresh = onRefresh
    ) {
        val scrollState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 32.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch { scrollState.scrollBy(-delta) }
                    },
                ),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = data) {
                Card(
                    modifier = Modifier.clickable { onTask(it) }.fillMaxWidth(),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = when (it.status) {
                            Task.TaskStatus.IN_WORK -> Color.Green
                            Task.TaskStatus.CLOSED -> Color.Gray
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 32.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(it.getTitle())
                    }
                }
            }
        }
    }

}

@Composable
fun Empty(
    reload: () -> Unit
) {
    InternetError(
        buttonText = "Обновить",
        icon = painterResource(Res.drawable.calendar),
        title = "Задач нет",
        description = null,
        onReloadClick = reload
    )
}
