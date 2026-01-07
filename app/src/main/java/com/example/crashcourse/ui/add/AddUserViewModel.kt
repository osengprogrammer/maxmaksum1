package com.example.crashcourse.ui.add

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.crashcourse.data.PhotoRepository
import com.example.crashcourse.viewmodel.FaceViewModel

class AddUserViewModel(
    private val photoRepository: PhotoRepository = PhotoRepository()
) : ViewModel() {

    fun registerUser(
        context: Context,
        state: AddUserUiState,
        faceViewModel: FaceViewModel,
        onSuccess: () -> Unit,
        onDuplicate: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // ✅ DOMAIN VALIDATION (NO UI FLAGS)
        if (state.name.isBlank()) {
            onError("Name is empty")
            return
        }

        if (state.studentId.isBlank()) {
            onError("Student ID is empty")
            return
        }

        if (state.embedding == null) {
            onError("Face embedding missing")
            return
        }

        if (state.capturedBitmap == null) {
            onError("Photo missing")
            return
        }

        // ✅ SAVE PHOTO
        val photoUrl = photoRepository.saveUserPhoto(
            context = context,
            bitmap = state.capturedBitmap,
            studentId = state.studentId.trim()
        )

        if (photoUrl == null) {
            onError("Failed to save photo")
            return
        }

        // ✅ REGISTER FACE
        faceViewModel.registerFace(
            studentId = state.studentId.trim(),
            name = state.name.trim(),
            embedding = state.embedding,
            photoUrl = photoUrl,
            onSuccess = onSuccess,
            onDuplicate = onDuplicate
        )
    }
}
