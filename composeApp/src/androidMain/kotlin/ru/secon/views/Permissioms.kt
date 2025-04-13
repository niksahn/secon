package ru.secon.views

import android.Manifest
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import tnsenergoo.composeapp.generated.resources.Res
import tnsenergoo.composeapp.generated.resources.set_permissions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun MissingPermissionsComponent(
    permissions: List<String>,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)
    if (permissionsState.revokedPermissions == permissions) {
        Toast.makeText(
            context,
            Res.string.set_permissions.key,
            Toast.LENGTH_SHORT
        ).show()
    }
    if (permissionsState.allPermissionsGranted) {
        content()
    } else {
        LaunchedEffect(permissionsState) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
}