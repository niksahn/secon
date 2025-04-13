package ru.secon.ui.views.bottomBar

import cafe.adriel.voyager.core.screen.Screen
import org.jetbrains.compose.resources.DrawableResource
import ru.secon.CurrentPlatform
import ru.secon.Platform
import ru.secon.ui.auth.profile.ProfileUi
import ru.secon.ui.map.MapUi
import ru.secon.ui.tasks.list.TasksUi
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.account_circle
import tnsenergoo.composeapp.generated.resources.map
import tnsenergoo.composeapp.generated.resources.task

enum class BottomBarDestination(
    val direction: Screen,
    val title: String,
    val icon: DrawableResource,
    val show: Boolean
) {
    Main(TasksUi, "Задачи", Res.drawable.task, true),
    Profile(ProfileUi, "Профиль", Res.drawable.account_circle, true),
    Map(MapUi, "Карта", Res.drawable.map, CurrentPlatform.current == Platform.Android)
}
