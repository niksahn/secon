package ru.secon.views

import androidx.compose.runtime.Composable

@Composable
expect fun MissingPermissionsComponent(permissions: List<String>, content: @Composable () -> Unit)