package ru.secon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.core.context.GlobalContext.startKoin
import ru.secon.data.di.dataModule
import ru.secon.ui.di.uiModule

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        startKoin {
            modules(listOf(dataModule, uiModule))
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { App() }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
