package ru.secon.ui.views.bottomBar

import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.DrawableResource
import ru.secon.ui.auth.AuthUi
import ru.secon.ui.tasks.TasksUi
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.ic_cyclone


enum class BottomBarDestination(
    val direction: Screen,
    val title: String,
    val icon: DrawableResource
) {
    Main(AuthUi, "Таски", Res.drawable.ic_cyclone),
    History(TasksUi, "Отчет", Res.drawable.ic_cyclone)
}
