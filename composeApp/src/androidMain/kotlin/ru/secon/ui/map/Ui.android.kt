package ru.secon.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.core.screen.Screen
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapType
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject
import ru.secon.R
import ru.secon.core.viewModel.base.subscribeScreenState

@Serializable
actual object MapUi : Screen {
    @Composable
    actual override fun Content() {
        val viewModel = koinInject<MapViewModel>()
        val state by viewModel.subscribeScreenState()
        YandexMap(points = state.points)
    }
}


// Composable функция
@Composable
fun YandexMap(
    modifier: Modifier = Modifier,
    points: List<Point> = emptyList()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                mapView = this
                map.mapType = MapType.MAP
                if (points.isNotEmpty()) {
                    map.move(
                        CameraPosition(points.first(), 5.0f, 0.0f, 0.0f)
                    )
                }
            }
        },
        update = { view ->
            // Обновляем метки при изменении points
            view.map.mapObjects.clear()
            points.forEach { point ->
                view.map.mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(ImageProvider.fromResource(context, R.drawable.icon))
                }
            }
        }
    )

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView?.onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView?.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> {}
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView = null
        }
    }
}
