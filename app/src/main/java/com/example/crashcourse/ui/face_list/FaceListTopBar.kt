@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.crashcourse.ui.face_list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun FaceListTopBar(
    count: Int
) {
    TopAppBar(
        title = { Text("Face Management ($count)") }
    )
}
