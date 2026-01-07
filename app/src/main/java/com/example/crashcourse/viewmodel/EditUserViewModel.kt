package com.example.crashcourse.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.utils.PhotoStorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EditUserUiState(
    val user: FaceEntity? = null,
    val name: String = "",
    val nameError: String? = null,
    val embedding: FloatArray? = null,
    val capturedBitmap: Bitmap? = null,
    val currentPhotoBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

class EditUserViewModel(
    private val faceViewModel: FaceViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUserUiState())
    val uiState: StateFlow<EditUserUiState> = _uiState

    /* -------------------------
     * Initialization
     * ------------------------- */

    fun loadUser(user: FaceEntity) {
        _uiState.value = EditUserUiState(
            user = user,
            name = user.name
        )

        loadExistingPhoto(user)
    }

    private fun loadExistingPhoto(user: FaceEntity) {
        user.photoUrl ?: return

        viewModelScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                PhotoStorageUtils.loadFacePhoto(user.photoUrl)
            }
            _uiState.value = _uiState.value.copy(
                currentPhotoBitmap = bmp
            )
        }
    }

    /* -------------------------
     * Name Handling
     * ------------------------- */

    fun onNameChanged(newName: String) {
        val error = validateName(newName)

        _uiState.value = _uiState.value.copy(
            name = newName,
            nameError = error
        )

        updateDirtyState()
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 50 -> "Name must be less than 50 characters"
            else -> null
        }
    }

    /* -------------------------
     * Face / Photo Updates
     * ------------------------- */

    fun onEmbeddingCaptured(embedding: FloatArray) {
        _uiState.value = _uiState.value.copy(
            embedding = embedding
        )
        updateDirtyState()
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            capturedBitmap = bitmap
        )
        updateDirtyState()
    }

    /* -------------------------
     * Dirty State
     * ------------------------- */

    private fun updateDirtyState() {
        val state = _uiState.value
        val user = state.user ?: return

        val dirty =
            state.name != user.name ||
            state.embedding != null ||
            state.capturedBitmap != null

        _uiState.value = state.copy(hasUnsavedChanges = dirty)
    }

    /* -------------------------
     * Save Logic
     * ------------------------- */

    fun save(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.nameError != null) {
            onError("Please fix validation errors")
            return
        }

        _uiState.value = state.copy(isProcessing = true)

        val updatedUser = user.copy(name = state.name.trim())
        val finalEmbedding = state.embedding ?: user.embedding
        val resizedBitmap = state.capturedBitmap?.let {
            PhotoStorageUtils.resizeBitmap(it, 800)
        }

        if (state.embedding != null || state.capturedBitmap != null) {
            faceViewModel.updateFaceWithPhoto(
                face = updatedUser,
                photoBitmap = resizedBitmap,
                embedding = finalEmbedding,
                onComplete = {
                    _uiState.value = _uiState.value.copy(isProcessing = false)
                    onSuccess()
                },
                onError = { msg ->
                    _uiState.value = _uiState.value.copy(isProcessing = false)
                    onError(msg)
                }
            )
        } else {
            faceViewModel.updateFace(updatedUser) {
                _uiState.value = _uiState.value.copy(isProcessing = false)
                onSuccess()
            }
        }
    }
}
