import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.startKoin
import ru.secon.App
import ru.secon.data.di.dataModule
import ru.secon.ui.di.uiModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(listOf(dataModule, uiModule))
    }
    val body = document.body ?: return
    ComposeViewport(body) {
        App()
    }
}
