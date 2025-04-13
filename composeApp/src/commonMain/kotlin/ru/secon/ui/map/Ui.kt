package ru.secon.ui.map

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.serialization.Serializable

@Serializable
expect object MapUi : Screen {
    @Composable
    override fun Content()
}