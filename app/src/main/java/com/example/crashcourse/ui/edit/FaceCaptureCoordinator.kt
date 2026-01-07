package com.example.crashcourse.ui.edit

import android.graphics.Bitmap
import androidx.compose.runtime.*
import com.example.crashcourse.scanner.FaceAnalyzerController
import com.example.crashcourse.scanner.FaceCaptureScreen
import com.example.crashcourse.scanner.CaptureMode

@Composable
fun FaceCaptureCoordinator(
    show: Boolean,
    captureMode: CaptureMode,
    onDismiss: () -> Unit,
    onEmbeddingCaptured: (FloatArray) -> Unit,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    if (!show) return

    val controller = remember { FaceAnalyzerController() }

    FaceCaptureScreen(
        mode = captureMode,
        controller = controller,
        onClose = onDismiss,
        onEmbeddingCaptured = { embedding ->
            onEmbeddingCaptured(embedding)
            onDismiss()
        },
        onPhotoCaptured = { bitmap ->
            onPhotoCaptured(bitmap)
            onDismiss()
        }
    )
}
