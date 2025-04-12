package ru.secon.core.utils

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.StringResource
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi

/** Сервис создания оповещений. */
class InAppNotificationService() : CoroutineScope by CoroutineScope(
    Dispatchers.Default
) {

    private val messageStack = ObservableMutableSet<Message>()
    val notificationFlow: Flow<Notification>

    init {
        notificationFlow = messageStack.observe()
            .mapNotNull { it.firstOrNull() }
            .map { it.toNotification() }
    }

    /** Добавить элемент в очередь сообщений. */
    fun send(message: Message) {
        messageStack.add(message)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun Message.toNotification(): Notification {
        return Notification(
            id = id.toString(),
            message = this,
            duration = 4_000L,
            onClose = { messageStack.remove(element = this) },
        )
    }

    /** Модель данных [Message] для создания оповещения [Notification].
     *
     * @param id Идентификатор сообщения.
     * @param titleId Строковый ресурс текста оповещения.
     * @param notificationType Тип оповещения для выбора шаблона [SmartwaySnackbarLayout].
     * @param action Операция выполняемая функциональной кнопкой шаблона [SmartwaySnackbarLayout] (опционально).
     * @param vibration Вибрация, сопровождающая оповещение (опционально).
     *
     */
    data class Message(
        val id: String = Random.nextBytes(200).toString(),
        val titleId: StringResource,
        val notificationType: SnackbarLayout.NotificationType,
        val title: String? = null,
        val vibration: Boolean = false,
    )

    /** Модель данных оповещения.
     *
     * @param message Модель данных сообщения [Message].
     * @param duration Время отображения оповещения.
     * @param onClose Закрыть оповещение.
     */
    @Immutable
    data class Notification(
        val id: String,
        val message: Message,
        val duration: Long,
        val onClose: () -> Unit,
    )
}