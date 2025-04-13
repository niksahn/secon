package ru.secon.views

import androidx.compose.runtime.Composable

@Composable
actual fun MissingPermissionsComponent(
    permissions: List<String>,
    content: @Composable () -> Unit,
) {
}