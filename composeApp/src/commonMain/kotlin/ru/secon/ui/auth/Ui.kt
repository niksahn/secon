package ru.secon.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.niksah.gagarin.utils.views.Spacer
import ru.secon.core.viewModel.base.subscribeEvents
import ru.secon.core.viewModel.base.subscribeScreenState
import ru.secon.ui.auth.admin.AdminAuthUi
import ru.secon.ui.tasks.TasksUi
import ru.secon.ui.views.Loading

data object AuthUi : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AuthViewModel>()
        val state = viewModel.subscribeScreenState()
        viewModel.subscribeEvents {
            when (it) {
                AuthEvent.OnHome -> navigator.push(TasksUi)
            }
        }
        Ui(
            state = state.value,
            onFirst = viewModel::inputFirstCode,
            onLogin = viewModel::auth,
            onSecond = viewModel::inputSecondCode,
            onAdminLogin = { navigator.push(AdminAuthUi) }
        )
    }

    @Composable
    private fun Ui(
        state: AuthState,
        onFirst: (String) -> Unit,
        onSecond: (String) -> Unit,
        onLogin: () -> Unit,
        onAdminLogin: () -> Unit
    ) {
        Scaffold {
            Box(
                Modifier.padding(it).fillMaxSize().verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Авторизация"
                    )
                    Spacer(24.dp)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.firstCode,
                        onValueChange = onFirst,
                        placeholder = { Text("Пароль") }
                    )
                    Spacer(24.dp)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.secondCode,
                        onValueChange = onSecond,
                        placeholder = { Text("Пароль") }
                    )
                    Spacer(32.dp)
                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        content = {
                            Text(text = "Войти")
                        },
                        onClick = onLogin
                    )
                    Spacer(16.dp)
                    Column(
                        Modifier.clickable(onClick = onAdminLogin).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        HorizontalDivider()
                        Spacer(8.dp)
                        Text(text = "Войти как администратор")
                    }
                }
            }
            Loading(state.loading)
        }
    }
}
