package com.example.crashcourse.ui.face_list

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.viewmodel.FaceViewModel

@Stable
class FaceListState(
    private val viewModel: FaceViewModel,
    private val facesState: State<List<FaceEntity>>
) {
    var searchQuery by mutableStateOf("")
        private set

    var editingFace by mutableStateOf<FaceEntity?>(null)
        private set

    val allFaces: List<FaceEntity>
        get() = facesState.value

    val filteredFaces: List<FaceEntity>
        get() = allFaces.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

    fun onSearchChange(value: String) {
        searchQuery = value
    }

    fun startEdit(face: FaceEntity) {
        editingFace = face
    }

    fun cancelEdit() {
        editingFace = null
    }

    fun saveEdit(updated: FaceEntity) {
        viewModel.updateFace(updated) {
            editingFace = null
        }
    }

    fun delete(face: FaceEntity) {
        viewModel.deleteFace(face)
    }

}

@Composable
fun rememberFaceListState(
    viewModel: FaceViewModel
): FaceListState {
    val faces = viewModel.faceList.collectAsStateWithLifecycle(emptyList())
    return remember(viewModel, faces.value) {
        FaceListState(viewModel, faces)
    }
}
