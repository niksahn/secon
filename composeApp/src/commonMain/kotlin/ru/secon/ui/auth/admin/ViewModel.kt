package ru.secon.ui.auth.admin

import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.domain.AuthApi
import ru.secon.domain.AuthService

data class AdminAuthState(
    val firstCode: String = "",
    val secondCode: String = "",
    val loading: Boolean = false
) : State()

sealed class AdminAuthEvent : Event() {
    data object OnHome : AdminAuthEvent()
}

class AdminAuthViewModel(
    private val authService: AuthService,
) : BaseViewModel<AdminAuthState, AdminAuthEvent>(AdminAuthState()) {
    fun inputFirstCode(code: String) {
        updateState { it.copy(firstCode = code) }
    }

    fun inputSecondCode(code: String) {
        updateState { it.copy(secondCode = code) }
    }

    fun auth() {
        launchViewModelScope {
            authService.login(
                AuthApi.AuthBody(
                    screenState.value.firstCode, screenState.value.secondCode
                )
            )
        }
    }
}