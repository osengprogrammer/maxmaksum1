package com.example.crashcourse.ui.add

import android.graphics.Bitmap
import com.example.crashcourse.scanner.CaptureMode

class FaceCaptureCoordinator {

    fun openEmbedding(state: AddUserUiState): AddUserUiState =
        state.copy(
            showFaceCapture = true,
            captureMode = CaptureMode.EMBEDDING
        )

    fun openPhoto(state: AddUserUiState): AddUserUiState =
        state.copy(
            showFaceCapture = true,
            captureMode = CaptureMode.PHOTO
        )

    fun onEmbeddingCaptured(
        state: AddUserUiState,
        embedding: FloatArray
    ): AddUserUiState =
        state.copy(
            embedding = embedding,
            showFaceCapture = false
        )

    fun onPhotoCaptured(
        state: AddUserUiState,
        bitmap: Bitmap
    ): AddUserUiState =
        state.copy(
            capturedBitmap = bitmap,
            showFaceCapture = false
        )
}
