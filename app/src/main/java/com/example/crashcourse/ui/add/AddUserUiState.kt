package com.example.crashcourse.ui.add

import android.graphics.Bitmap
import com.example.crashcourse.scanner.CaptureMode   // ✅ ADD THIS

data class AddUserUiState(
    val name: String = "",
    val studentId: String = "",
    val embedding: FloatArray? = null,
    val capturedBitmap: Bitmap? = null,
    val isSubmitting: Boolean = false,
    val isSaved: Boolean = false,
    val showFaceCapture: Boolean = false,
    val captureMode: CaptureMode = CaptureMode.EMBEDDING
) {
    val canSubmit: Boolean
        get() = name.isNotBlank()
                && studentId.isNotBlank()
                && embedding != null
                && capturedBitmap != null
                
}
