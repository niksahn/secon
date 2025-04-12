package com.niksah.gagarin.screens.camera

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
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
import com.niksah.gagarin.utils.base.subscribeEvents
import com.niksah.gagarin.utils.base.subscribeScreenState
import com.niksah.gagarin.utils.views.Alert
import com.niksah.gagarin.views.MissingPermissionsComponent
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CameraState
import com.ujizin.camposer.state.rememberCamSelector
import com.ujizin.camposer.state.rememberCameraState
import gagarinhak.composeapp.generated.resources.Res
import gagarinhak.composeapp.generated.resources.arrow_back
import moe.tlaster.precompose.koin.koinViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
internal actual fun Camera(
    makedPhoto: () -> Unit,
    onBack: () -> Unit
) {
    var showError by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val viewModel = koinViewModel(CameraViewModel::class)
    val state by viewModel.subscribeScreenState()
    MissingPermissionsComponent {
        val cameraState = rememberCameraState()
        val camSelector by rememberCamSelector(CamSelector.Back)
        viewModel.subscribeEvents {
            when (it) {
                is CameraEvent.Failure -> {
                    showError = it.message ?: "ERROR"
                }

                CameraEvent.MakedPhoto -> {
                    Toast.makeText(
                        context,
                        "Фото загружено",
                        Toast.LENGTH_SHORT
                    ).show()
                    makedPhoto()
                }
            }
        }
        if (state.showScanner) {
            Scanner(
                onResult = viewModel::onResultScan,
                onBack = onBack,
                holdError = viewModel::onNotEnableScanner
            )
        } else {
            CamUi(
                cameraState = cameraState,
                camSelector = camSelector,
                makingPhoto = state.makingPhoto,
                takePicture = { viewModel.takePicture(cameraState) },
                goBack = onBack
            )
        }
        Alert(showError = showError) {
            showError = ""
            onBack()
        }
    }
}

@Composable
fun CamUi(
    cameraState: CameraState,
    makingPhoto: Boolean,
    camSelector: CamSelector,
    takePicture: () -> Unit,
    goBack: () -> Unit
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
                indication = rememberRipple(bounded = true),
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
