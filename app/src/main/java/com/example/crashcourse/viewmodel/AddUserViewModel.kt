package com.example.crashcourse.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.crashcourse.ui.add.AddUserUiState
import com.example.crashcourse.utils.PhotoStorageUtils

class AddUserViewModel : ViewModel() {

    fun registerUser(
        context: Context,
        state: AddUserUiState,
        faceViewModel: FaceViewModel,
        onSuccess: () -> Unit,
        onDuplicate: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // -------------------------
        // Validation
        // -------------------------
        when {
            state.name.isBlank() -> {
                onError("Name is empty")
                return
            }
            state.studentId.isBlank() -> {
                onError("Student ID is empty")
                return
            }
            state.embedding == null -> {
                onError("Face embedding missing")
                return
            }
            state.capturedBitmap == null -> {
                onError("Photo missing")
                return
            }
        }

        // -------------------------
        // Safe extraction
        // -------------------------
        val name = state.name.trim()
        val studentId = state.studentId.trim()
        val embedding = state.embedding!!
        val bitmap = state.capturedBitmap!!

        // -------------------------
        // Save photo to disk
        // -------------------------
        val photoPath: String = PhotoStorageUtils.saveFacePhoto(
            context = context.applicationContext,
            bitmap = bitmap,
            studentId = studentId
        ) ?: run {
            onError("Failed to save photo")
            return
        }

        // -------------------------
        // Register face ONCE
        // -------------------------
        faceViewModel.registerFace(
            studentId = studentId,
            name = name,
            embedding = embedding,
            photoUrl = photoPath,
            className = "",
            subClass = "",
            grade = "",
            subGrade = "",
            program = "",
            role = "",
            onSuccess = onSuccess,
            onDuplicate = onDuplicate,
            onError = onError
        )
    }
}
