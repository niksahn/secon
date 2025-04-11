package ru.secon.core.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tnsenergoo.composeapp.generated.resources.Res
import kotlin.time.Duration.Companion.milliseconds

object SnackbarLayout {

    /** Тип иконки в шаблоне оповещения. */
    enum class IconType { SMALL, LARGE }

    /** Тип оповещения. */
    sealed class NotificationType {

        /** Оповещение об ошибке. */
        data object Error : NotificationType()

        /** Оповещение об успешной опрации. */
        data class Approve(
            val iconType: IconType = IconType.SMALL,
            val iconId: DrawableResource = Res.drawable.approve,
        ) : NotificationType()
    }

    @Composable
    fun OperationInfo(
        modifier: Modifier = Modifier,
        iconId: DrawableResource? = null, // Используем Multiplatform Resources для Drawable
        iconType: IconType,
        button: @Composable (() -> Unit)? = null,
        information: String,
        onClose: () -> Unit,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Если иконка предоставлена
                iconId?.let {
                    when (iconType) {
                        IconType.SMALL -> {
                            Box(
                                modifier = Modifier.size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(iconId), // Multiplatform Resources
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconType.LARGE -> {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(iconId), // Multiplatform Resources
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp)) // Исправленный отступ
                        }
                    }
                }

                Text(
                    modifier = Modifier
                        .weight(1f, false)
                        .fillMaxWidth(),
                    text = information,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Если есть кнопка, отображаем ее
                if (button != null) {
                    button()
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(DrawableResource.close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun NotificationInfo(
        modifier: Modifier = Modifier,
        iconId: DrawableResource =Res.drawable.error, // Используем Multiplatform Resources для Drawable
        information: String,
        onClose: () -> Unit,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f, false)
                            .fillMaxWidth(),
                        text = information,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Notification(notification: InAppNotificationService.Notification) {
        val coroutineScope = rememberCoroutineScope()
        var isNotificationVisible by remember { mutableStateOf(value = false) }
        fun close(notification: InAppNotificationService.Notification) {
            isNotificationVisible = false
            coroutineScope.launch {
                delay(400.milliseconds)
                notification.onClose()
            }
        }

        AnimatedVisibility(
            visible = isNotificationVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
        ) {
                when (val type = notification.message.notificationType) {
                    is NotificationType.Approve ->
                        OperationInfo(
                            modifier = Modifier.padding(16.dp),
                            information = notification.message.title ?: stringResource(notification.message.titleId),
                            iconId = type.iconId,
                            iconType = type.iconType,
                            onClose = { close(notification) },
                        )

                    NotificationType.Error ->
                        NotificationInfo(
                            modifier = Modifier.padding(16.dp),
                            information = notification.message.title ?: stringResource(notification.message.titleId),
                            onClose = { close(notification) },
                        )
                }
                 }
        LaunchedEffect(notification.id) {
            val localNotification = notification
            isNotificationVisible = true
            delay(localNotification.duration)
            if (isNotificationVisible) close(localNotification)
        }
    }
}
