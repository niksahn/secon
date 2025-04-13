package ru.secon.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.secon.core.network.NetworkFailure
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.no_back_title
import tnsenergoo.composeapp.generated.resources.no_connection_description
import tnsenergoo.composeapp.generated.resources.no_connection_title
import tnsenergoo.composeapp.generated.resources.update
import tnsenergoo.composeapp.generated.resources.warning


@Composable
fun Loading(loading: Boolean) {
    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface.copy(0.7f))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }
    }
}

@Composable
fun ErrorState(
    error: NetworkFailure,
    onUpdateState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (error) {
            NetworkFailure.NO_CONNECTION ->
                InternetError(
                    title = stringResource(Res.string.no_connection_title),
                    description = stringResource(Res.string.no_connection_description),
                    buttonText = stringResource(Res.string.update),
                    onReloadClick = onUpdateState
                )

            NetworkFailure.ERROR ->
                InternetError(
                    title = stringResource(Res.string.no_back_title),
                    description = stringResource(Res.string.no_connection_description),
                    buttonText = stringResource(Res.string.update),
                    onReloadClick = onUpdateState
                )
        }
    }
}

@Composable
fun InternetError(
    buttonText: String?,
    icon: Painter = painterResource(Res.drawable.warning),
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    onReloadClick: () -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .width(96.dp)
                        .height(92.dp),
                    painter = icon,
                    contentDescription = null
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (description != null) {
                        Text(
                            text = description,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            buttonText?.let {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    content = { Text(buttonText) },
                    onClick = onReloadClick
                )
            }
        }
    }
}
