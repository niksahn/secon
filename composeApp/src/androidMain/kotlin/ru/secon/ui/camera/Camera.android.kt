package ru.secon.ui.camera

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.niksah.gagarin.utils.views.Alert
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.ui.tasks.info.Image
import ru.secon.ui.tasks.info.TaskInfoViewModel
import ru.secon.ui.views.FileChooser
import ru.secon.views.MissingPermissionsComponent
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.arrow_back
import tnsenergoo.composeapp.generated.resources.file_earmark_arrow_up

actual class CameraUi actual constructor(image: Image) : Screen {
    actual val imageVal: Image = image

    @Composable
    actual override fun Content() {
        var showError by remember {
            mutableStateOf("")
        }
        var showFilePicker by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val viewModel = koinInject<CameraViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val tasksInfoModel = navigator.koinNavigatorScreenModel<TaskInfoViewModel>()
        val state by viewModel.subscribeScreenState()
        MissingPermissionsComponent(listOf(Manifest.permission.CAMERA)) {
            val cameraState = rememberCameraState()
            val camSelector by rememberCamSelector(CamSelector.Back)
            viewModel.subscribeEvents {
                when (it) {
                    is CameraEvent.Failure -> {
                        showError = it.message ?: "ERROR"
                    }

                    is CameraEvent.MakedPhoto -> {
                        Toast.makeText(
                            context,
                            "Фото загружено",
                            Toast.LENGTH_SHORT
                        ).show()
                        when (imageVal) {
                            Image.First -> tasksInfoModel.setFirstImage(it.photo)
                            Image.Second -> tasksInfoModel.setSecondImage(it.photo)
                        }
                        navigator.pop()
                    }
                }
            }
            CamUi(
                cameraState = cameraState,
                camSelector = camSelector,
                makingPhoto = state.makingPhoto,
                takePicture = { viewModel.takePicture(cameraState) },
                goBack = { navigator.pop() },
                onFile = { showFilePicker = true }

            )
            Alert(showError = showError) {
                showError = ""
                navigator.pop()
            }
            FileChooser(
                showFilePicker = showFilePicker,
                fileType = listOf("jpg", "png"),
                close = { showFilePicker = false },
                loadFile = {
                    showFilePicker = false
                    if (it != null) {
                        viewModel.loadFile(it)
                    }
                }
            )
        }
    }

    @Composable
    fun CamUi(
        cameraState: CameraState,
        makingPhoto: Boolean,
        camSelector: CamSelector,
        takePicture: () -> Unit,
        goBack: () -> Unit,
        onFile: () -> Unit
    ) {
        Scaffold {
            CameraPreview(
                modifier = Modifier.padding(it),
                cameraState = cameraState,
                camSelector = camSelector,
            ) {
                if (makingPhoto) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                } else {
                    Column {
                        Icon(
                            painter = painterResource(resource = Res.drawable.arrow_back),
                            contentDescription = null,
                            modifier = Modifier
                                .clickable(onClick = goBack, role = Role.Button)
                                .padding(top = 64.dp, start = 32.dp)
                                .background(color = Color.White.copy(0.2f), shape = CircleShape)
                        )
                        Box(modifier = Modifier.weight(1F))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(
                                    modifier = Modifier
                                        .clickable(onClick = onFile)
                                        .size(84.dp),
                                    painter = painterResource(Res.drawable.file_earmark_arrow_up),
                                    contentDescription = null,
                                )
                                Button(
                                    onClick = takePicture,
                                    color = Color.White,
                                    size = 84.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Button(
        onClick: () -> Unit,
        color: Color,
        size: Dp,
        enabled: Boolean = true,
        contentPaddingValues: PaddingValues = PaddingValues(0.dp),
        content: @Composable BoxScope.() -> Unit = {},
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(if (pressed) 0.85F else 1F, label = "")

        Box(
            modifier = Modifier
                .scale(scale)
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    indication = ripple(bounded = true),
                    interactionSource = interactionSource,
                    onClick = onClick,
                )
                .size(size)
                .background(color)
                .padding(contentPaddingValues),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}
