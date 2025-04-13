package ru.secon.ui.tasks.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.Qualifier
import org.jetbrains.compose.resources.painterResource
import org.koin.core.qualifier.QualifierValue
import ru.secon.CurrentPlatform
import ru.secon.Platform
import ru.secon.core.monads.Operation
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.data.Task
import ru.secon.ui.camera.CameraUi
import ru.secon.ui.tasks.list.TasksUi
import ru.secon.ui.views.Checkable
import ru.secon.ui.views.FileChooser
import ru.secon.ui.views.Loading
import ru.secon.ui.views.Radio
import ru.secon.ui.views.SelectList
import ru.secon.views.MissingPermissionsComponent
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.arrow_back
import tnsenergoo.composeapp.generated.resources.file_earmark_arrow_up

enum class Image {
    First, Second
}

class TaskQualifier(
    val task: Task
) : Qualifier, org.koin.core.qualifier.Qualifier {
    override val value: QualifierValue
        get() = task.toString()
}

@Serializable
data class TasksInfoUi(val task: Task) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = navigator.koinNavigatorScreenModel<TaskInfoViewModel>()
        val state = viewModel.subscribeScreenState()
        LaunchedEffect(Unit) {
            viewModel.setTask(task)
        }
        viewModel.subscribeEvents {
            when (it) {
                is TaskInfoEvent.ActFormed -> {
                    viewModel.clear()
                    navigator.replaceAll(TasksUi)
                }
            }
        }

        var showFilePicker by remember { mutableStateOf<Image?>(null) }
        val fileType = listOf("jpg", "png")
        FileChooser(
            showFilePicker = showFilePicker != null,
            fileType = fileType,
            close = { showFilePicker = null },
            loadFile = {
                when {
                    it == null -> {}
                    showFilePicker == Image.First -> viewModel.setFirstImage(it)
                    showFilePicker == Image.Second -> viewModel.setSecondImage(it)
                }
                showFilePicker = null
            }
        )

        Scaffold(
            topBar = {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(
                        modifier = Modifier.clickable {
                            viewModel.clear()
                            navigator.replaceAll(TasksUi)
                        },
                        painter = painterResource(Res.drawable.arrow_back),
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = task.getTitle()
                    )
                }
            },
            bottomBar = {
                MissingPermissionsComponent(
                    permissions = listOf(
                        "android.permission.ACCESS_COARSE_LOCATION",
                        "android.permission.ACCESS_FINE_LOCATION"
                    )
                ) {
                    when (state.value.task?.status) {
                        Task.TaskStatus.IN_WORK ->
                            Button(
                                modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                                onClick = viewModel::formAct,
                                content = {
                                    Text("Закрыть акт")
                                }
                            )

                        Task.TaskStatus.CLOSED ->
                            Button(
                                modifier = Modifier.fillMaxWidth().padding(16.dp).height(64.dp),
                                onClick = viewModel::loadAct,
                                content = {
                                    Text("Скачать акт")
                                }
                            )

                        null -> {}
                    }
                }
            }
        ) {
            val scrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .padding(it)
                    .padding(top = 32.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            coroutineScope.launch { scrollState.scrollBy(-delta) }
                        },
                    )
                    .verticalScroll(scrollState)
            ) {
                val data = state.value.task
                when {
                    data == null -> Loading(true)
                    data.status == Task.TaskStatus.IN_WORK ->
                        FormingAct(
                            state = state.value,
                            onLoadFirst = {
                                val image = Image.First
                                when (CurrentPlatform.current) {
                                    Platform.Android -> navigator.push(CameraUi(image))
                                    Platform.Pc -> showFilePicker = image
                                }
                            },
                            onLoadSecond = {
                                val image = Image.Second
                                when (CurrentPlatform.current) {
                                    Platform.Android -> navigator.push(CameraUi(image))
                                    Platform.Pc -> showFilePicker = image
                                }
                            },
                            onStop = viewModel::setStop,
                            onViolation = viewModel::setViolation,
                            onComunate = viewModel::setCommunate,
                            onSelf = viewModel::setNotSelf,
                            onDevice = viewModel::setDevice,
                            onReason = viewModel::setReason,
                            onResult = viewModel::setResult,
                            onElectricity = viewModel::setElectricity,
                            onClient = viewModel::setClient,
                            onLocation = viewModel::setLocation,
                            onLocationPlace = viewModel::setLocation
                        )

                    data.status == Task.TaskStatus.CLOSED ->
                        FormedAct(
                            state = state.value,
                            onLoad = {}
                        )
                }
            }
        }
    }

    @Composable
    private fun FormingAct(
        state: TaskInfoState,
        onLoadFirst: () -> Unit,
        onLoadSecond: () -> Unit,
        onStop: (Boolean) -> Unit,
        onComunate: (Boolean) -> Unit,
        onSelf: (Boolean) -> Unit,
        onViolation: (Boolean) -> Unit,
        onDevice: (String) -> Unit,
        onReason: (String) -> Unit,
        onClient: (String) -> Unit,
        onResult: (TaskInfoState.ActAdditional.New.ResultTask) -> Unit,
        onElectricity: (TaskInfoState.ActAdditional.New.Electricity) -> Unit,
        onLocation: (Place) -> Unit,
        onLocationPlace: (String) -> Unit
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Фото до")
            ImageInfo(onLoadFirst, state.firstImage)
            Text("Фото после")
            ImageInfo(onLoadSecond, state.secondImage)
            when (state.additional) {
                is TaskInfoState.ActAdditional.New ->
                    NewAct(
                        state.additional,
                        onStop = onStop,
                        onCommunate = onComunate,
                        onDevice = onDevice,
                        onResult = onResult,
                        onReason = onReason,
                        onElectricity = onElectricity,
                        onClient = onClient,
                        onLocation = onLocation,
                        onLocationPlace = onLocationPlace
                    )

                is TaskInfoState.ActAdditional.Previously ->
                    PreviousAct(
                        state.additional,
                        onSelf = onSelf,
                        onViolation = onViolation,
                        onCommunate = onComunate,
                        onDevice = onDevice,
                        onLocation = onLocation,
                        onLocationPlace = onLocationPlace,
                    )

                null -> {}
            }
        }
    }

    @Composable
    private fun NewAct(
        values: TaskInfoState.ActAdditional.New,
        onStop: (Boolean) -> Unit,
        onCommunate: (Boolean) -> Unit,
        onResult: (TaskInfoState.ActAdditional.New.ResultTask) -> Unit,
        onDevice: (String) -> Unit,
        onClient: (String) -> Unit,
        onReason: (String) -> Unit,
        onElectricity: (TaskInfoState.ActAdditional.New.Electricity) -> Unit,
        onLocation: (Place) -> Unit,
        onLocationPlace: (String) -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Radio(
                item = Checkable(
                    checked = values.stop,
                    type = Unit,
                    text = "Приостановить предоставление услуги"
                ),
                onSelect = { onStop(true) }
            )
            Radio(
                item = Checkable(
                    checked = !values.stop,
                    type = Unit,
                    text = "Возобновить предоставление услуги"
                ),
                onSelect = { onStop(false) }
            )
            Switcher(
                onCheck = onCommunate,
                checked = values.communat,
                text = "Комутационный аппарат"
            )
            Text("Тип ограничения:")
            SelectList(
                modifier = Modifier.padding(start = 16.dp),
                items = TaskInfoState.ActAdditional.New.ResultTask.entries.map {
                    Checkable(
                        it, text = it.toString(), checked = values.result == it
                    )
                },
                onSelect = onResult
            )
            if (values.notStartReason != null) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Причина по которой не введено") },
                    value = values.notStartReason, onValueChange = onReason
                )
            }
            Text("Предоставление услуги:")
            SelectList(
                modifier = Modifier.padding(start = 16.dp),
                items = TaskInfoState.ActAdditional.New.Electricity.entries.map {
                    Checkable(
                        it, text = it.toString(), checked = values.electricity == it
                    )
                },
                onSelect = onElectricity
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Полные ФИО клиента") },
                value = values.clientName, onValueChange = onClient
            )
            Text("Прибор расположен:")
            SelectList(
                modifier = Modifier.padding(start = 16.dp),
                items = Place.entries.map {
                    Checkable(
                        it, text = it.toString(), checked = values.location == it
                    )
                },
                onSelect = onLocation
            )
            if (values.locationPlace != null) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Расположение прибора") },
                    value = values.locationPlace, onValueChange = onLocationPlace
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Тип и номер прибора учета") },
                value = values.deviceParams, onValueChange = onDevice
            )
        }
    }

    @Composable
    private fun PreviousAct(
        values: TaskInfoState.ActAdditional.Previously,
        onSelf: (Boolean) -> Unit,
        onViolation: (Boolean) -> Unit,
        onCommunate: (Boolean) -> Unit,
        onDevice: (String) -> Unit,
        onLocation: (Place) -> Unit,
        onLocationPlace: (String) -> Unit
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Switcher(
                onCheck = onSelf,
                checked = values.notSelf,
                text = "Установлено самовольное подключение"
            )
            Switcher(
                onCheck = onViolation,
                checked = values.violation,
                text = "Нарушение потребителем ограничения"
            )
            Switcher(
                onCheck = onCommunate,
                checked = values.communat,
                text = "Комутационный аппарат"
            )
            Text("Прибор расположен:")
            SelectList(
                modifier = Modifier.padding(start = 16.dp),
                items = Place.entries.map {
                    Checkable(
                        it, text = it.toString(), checked = values.location == it
                    )
                },
                onSelect = onLocation
            )
            if (values.locationPlace != null) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Расположение прибора") },
                    value = values.locationPlace, onValueChange = onLocationPlace
                )
            }
            OutlinedTextField(value = values.deviceParams, onValueChange = onDevice)
        }
    }

    @Composable
    private fun PlaceHolder(
        onClick: () -> Unit,
        enable: Boolean = true
    ) {
        Image(
            modifier = Modifier.height(140.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = enable, onClick = onClick),
            painter = painterResource(Res.drawable.file_earmark_arrow_up),
            contentDescription = null
        )
    }

    @Composable
    private fun FormedAct(
        state: TaskInfoState,
        onLoad: () -> Unit
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Фото до")
            ImageInfo({}, state.firstImage, false)
            Text("Фото после")
            ImageInfo({}, state.secondImage, false)
        }
    }

    @Composable
    private fun ImageInfo(
        onClick: () -> Unit,
        imageInfo: Operation<ImageBitmap, Unit>,
        enable: Boolean = true,
    ) {
        when (imageInfo) {
            is Operation.Failure ->
                Box(modifier = Modifier.size(120.dp)) {
                    PlaceHolder(onClick, enable)
                }

            Operation.Preparing ->
                Box(modifier = Modifier.size(120.dp)) {
                    Loading(true)
                }

            is Operation.Success ->
                Image(
                    bitmap = imageInfo.value,
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp, 150.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
        }
    }

    @Composable
    private fun Switcher(
        text: String,
        onCheck: (Boolean) -> Unit,
        checked: Boolean
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Switch(checked = checked, onCheckedChange = onCheck)
            Text(
                text,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}
