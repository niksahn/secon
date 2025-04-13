package com.niksah.gagarin.utils.views.bottomBar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import org.jetbrains.compose.resources.painterResource
import ru.secon.theme.SelectedIcon
import ru.secon.theme.UnSelectedIcon
import ru.secon.ui.views.bottomBar.BottomBarDestination


@Composable
fun BottomBar(
    navController: Navigator,
) {
    val currentDestination = navController.lastItem
    Column {
        NavigationBar(
            containerColor = Color.White
        ) {
            BottomBarDestination.entries
                .filter { it.show }
                .forEach { destination ->
                    val selected = currentDestination == destination.direction
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.push(destination.direction)
                        },
                        icon = {
                            Icon(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(32.dp),
                                painter = painterResource(destination.icon),
                                contentDescription = destination.title,
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                color = Color.Black,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SelectedIcon,
                            unselectedIconColor = UnSelectedIcon,
                        ),
                    )
                }
        }
    }
}
