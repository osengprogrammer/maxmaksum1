package com.example.crashcourse.ui.face_list

import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.viewmodel.FaceViewModel

@Composable
fun FaceListScreen(
    viewModel: FaceViewModel = viewModel(),
    onEditUser: (FaceEntity) -> Unit
) {
    val state = rememberFaceListState(viewModel)

    Scaffold(
        topBar = {
            FaceListTopBar(
                count = state.allFaces.size
            )
        }
    ) { padding ->
        FaceListContent(
            state = state,
            padding = padding,
            onEditUser = onEditUser
        )
    }

    FaceEditDialog(
        editingFace = state.editingFace,
        onDismiss = state::cancelEdit,
        onSave = state::saveEdit
    )
}
