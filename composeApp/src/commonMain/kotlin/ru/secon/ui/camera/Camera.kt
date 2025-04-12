package ru.secon.ui.camera

import androidx.compose.runtime.Composable

@Composable
internal expect fun Camera(makedPhoto: () -> Unit, onBack: () -> Unit)