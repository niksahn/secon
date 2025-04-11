package ru.secon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.niksah.gagarin.utils.views.bottomBar.BottomBar
import ru.secon.theme.AppTheme
import ru.secon.ui.auth.AuthUi
import ru.secon.ui.views.bottomBar.BottomBarDestination

@Composable
internal fun App() = AppTheme {
    Navigator(AuthUi) { navigator ->
        Scaffold(
            bottomBar = {
                if (BottomBarDestination.entries.any { it.direction == navigator.lastItemOrNull } && CurrentPlatform.current == Platform.Android) {
                    BottomBar(navigator)
                }
            },
            content = { Box(Modifier.padding(it).imePadding()) { CurrentScreen() } },
        )
    }
}

