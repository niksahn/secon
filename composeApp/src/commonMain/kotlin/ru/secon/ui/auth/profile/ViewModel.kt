package ru.secon.ui.auth.profile

import ru.secon.core.viewModel.base.BaseViewModel
import ru.secon.core.viewModel.base.Event
import ru.secon.core.viewModel.base.State
import ru.secon.domain.AuthService

data object AdminAuthState : State()

sealed class AdminAuthEvent : Event()

class ProfileViewModel(
    private val authService: AuthService,
) : BaseViewModel<AdminAuthState, AdminAuthEvent>(AdminAuthState) {
    fun logout() = authService.logout()
}