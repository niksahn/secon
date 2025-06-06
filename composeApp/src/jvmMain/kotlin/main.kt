import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.startKoin
import ru.secon.App
import ru.secon.core.domainJvmModule
import ru.secon.data.di.dataModule
import ru.secon.ui.di.uiModule
import java.awt.Dimension

fun main() = application {
    startKoin {
        modules(listOf(dataModule, uiModule, domainJvmModule))
    }
    Window(
        title = "TNSEnergoo",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
