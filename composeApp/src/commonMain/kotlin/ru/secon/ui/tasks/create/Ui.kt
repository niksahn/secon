package ru.secon.ui.tasks.create

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.ui.tasks.list.TaskViewModel
import ru.secon.ui.views.FileChooser
import ru.secon.ui.views.SelectList
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.arrow_back

@Serializable
data object CreateTasksUi : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<CreateTaskViewModel>()
        val state = viewModel.subscribeScreenState()
        var showFilePicker by remember { mutableStateOf(false) }
        val fileType = listOf("xlsx", "xls")
        val taskModel = navigator.koinNavigatorScreenModel<TaskViewModel>()
        viewModel.subscribeEvents {
            when (it) {
                is CreateTaskEvent.OpenTask -> TODO()
                CreateTaskEvent.ExelLoaded -> {
                    taskModel.reload()
                    navigator.pop()
                }
            }
        }
        FileChooser(
            showFilePicker = showFilePicker,
            fileType = fileType,
            close = { showFilePicker = false },
            loadFile = {
                showFilePicker = false
                if (it != null) viewModel.loadFromExel(it)
            }
        )

        Scaffold(
            topBar = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Icon(
                        modifier = Modifier.clickable { navigator.pop() },
                        painter = painterResource(Res.drawable.arrow_back),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = "Создание задачи"
                    )
                }
            },
            bottomBar = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        content = {
                            Text(text = "Сформировать задачу")
                        },
                        onClick = viewModel::makeTask
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        content = {
                            Text(text = "Загрузить задачи из файла")
                        },
                        onClick = { showFilePicker = true }
                    )
                }
            }
        ) {
            val scroll = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(horizontal = 24.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch { scroll.scrollBy(-delta) }
                        },
                    )
                    .verticalScroll(scroll)
            ) {
                OutlinedTextField(
                    placeholder = { Text("Город") },
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value.city,
                    onValueChange = viewModel::setCity
                )
                OutlinedTextField(
                    placeholder = { Text("Улица") },
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value.street,
                    onValueChange = viewModel::setStreet
                )
                OutlinedTextField(
                    placeholder = { Text("Дом") },
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value.house,
                    onValueChange = viewModel::setHouse
                )
                OutlinedTextField(
                    placeholder = { Text("Квартира") },
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value.flat,
                    onValueChange = viewModel::setFlat
                )
                OutlinedTextField(
                    placeholder = { Text("Комната") },
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value.room,
                    onValueChange = viewModel::setRoom
                )
                OutlinedTextField(
                    placeholder = { Text("Тип устройства") },
                    modifier = Modifier.fillMaxWidth(),
                    value = state.value.device,
                    onValueChange = viewModel::setDevice
                )
                SelectList(
                    modifier = Modifier.fillMaxWidth(),
                    items = state.value.types,
                    onSelect = viewModel::setType
                )
            }
        }
    }
}
