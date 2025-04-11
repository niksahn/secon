package ru.secon.ui.auth

import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.domain.AuthApi
import ru.secon.domain.AuthService

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
            println("AAAAAAAAAA")
            val response = authService.login(
                AuthApi.AuthBody(
                    screenState.value.firstCode, screenState.value.secondCode
                )
            )
            updateState { it.copy(loading = false) }
           if  trySendEvent()
        }
    }
}