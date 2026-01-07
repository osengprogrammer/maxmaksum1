package com.example.crashcourse.scanner

import android.Manifest
import androidx.compose.runtime.Composable
import com.example.crashcourse.ui.PermissionsHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionGate(
    content: @Composable () -> Unit
) {
    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA)

    PermissionsHandler(permissionState = cameraPermissionState) {
        content()
    }
}
