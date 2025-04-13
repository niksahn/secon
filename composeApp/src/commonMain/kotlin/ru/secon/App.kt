package ru.secon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.niksah.gagarin.utils.views.bottomBar.BottomBar
import org.koin.compose.koinInject
import ru.secon.core.utils.InAppNotificationService
import ru.secon.core.utils.SnackbarLayout
import ru.secon.domain.AuthService
import ru.secon.theme.AppTheme
import ru.secon.ui.auth.usual.AuthUi
import ru.secon.ui.tasks.list.TasksUi
import ru.secon.ui.views.bottomBar.BottomBarDestination

@Composable
internal fun App(
    inAppNotificationService: InAppNotificationService = koinInject<InAppNotificationService>(),
    authService: AuthService = koinInject<AuthService>()
) = AppTheme {
    val isAuth = authService.isLogin()
    Navigator(if (isAuth) TasksUi else AuthUi) { navigator ->
        Scaffold(
            bottomBar = {
                if (BottomBarDestination.entries.any { it.direction == navigator.lastItemOrNull }) {
                    BottomBar(navigator)
                }
            },
            content = {
                val notification by inAppNotificationService.notificationFlow.collectAsState(initial = null)
                Box(Modifier.padding(it).imePadding()) {
                    CurrentScreen()
                    notification?.let { SnackbarLayout.Notification(notification = it) }
                }
            },
        )
        LaunchedEffect(Unit) {
            authService.observeIsLogin()
                .collect {
                    if (!it) {
                        navigator.replaceAll(AuthUi)
                    } else {
                        navigator.replaceAll(TasksUi)
                    }
                }
        }
    }
}

