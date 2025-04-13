package ru.secon

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import com.yandex.mapkit.MapKitFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.secon.data.di.dataModule
import ru.secon.ui.di.uiModule
import ru.secon.ui.domainAndroidModule
import ru.secon.ui.uiAndroidModule

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        startKoin {
            androidContext(applicationContext)
            modules(listOf(dataModule, uiModule, uiAndroidModule, domainAndroidModule))
        }
        MapKitFactory.setApiKey("f5dccc64-13aa-4ab3-835e-e4d6b30eac2b")


        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    lightScrim = androidx.compose.ui.graphics.Color.Transparent.toArgb(),
                    darkScrim = Color.BLACK
                )
        )
        setContent { App() }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
