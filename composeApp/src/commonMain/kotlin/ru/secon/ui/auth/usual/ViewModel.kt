package ru.secon.ui.auth.usual

import ru.secon.core.network.isSuccess
import ru.secon.core.utils.InAppNotificationService
import ru.secon.core.utils.SnackbarLayout
import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.domain.AuthApi
import ru.secon.domain.AuthService
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.loading_failed_title

data class AuthState(
    val firstCode: String = "",
    val secondCode: String = "",
    val loading: Boolean = false
) : State()

sealed class AuthEvent : Event() {
    data object OnHome : AuthEvent()
}

class AuthViewModel(
    private val authService: AuthService,
    private val notificationService: InAppNotificationService
) : BaseViewModel<AuthState, AuthEvent>(AuthState()) {
    fun inputFirstCode(code: String) {
        updateState { it.copy(firstCode = code) }
    }

    fun inputSecondCode(code: String) {
        updateState { it.copy(secondCode = code) }
    }

    fun auth() {
        launchViewModelScope {
            updateState { it.copy(loading = true) }
            val response = authService.login(
                AuthApi.AuthBody(
                    screenState.value.firstCode, screenState.value.secondCode
                )
            )
            updateState { it.copy(loading = false) }
            if (response.isSuccess) trySendEvent(AuthEvent.OnHome) else notificationService.send(
                InAppNotificationService.Message(
                    notificationType = SnackbarLayout.NotificationType.Error,
                    titleId = Res.string.loading_failed_title,
                )
            )
        }
    }
}