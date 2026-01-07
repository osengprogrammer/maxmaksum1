package com.example.crashcourse.scanner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.crashcourse.ml.FaceAnalyzer
import com.example.crashcourse.ui.FaceOverlay

/**
 * FaceScanner
 *
 * Thin composable wrapper around CameraX + FaceAnalyzer.
 */
@Composable
fun FaceScanner(
    controller: FaceAnalyzerController,
    useBackCamera: Boolean = false,
    modifier: Modifier = Modifier
) {
    // ✅ remember ONLY creates the analyzer object
    val analyzer = remember(controller) {
        FaceAnalyzer(controller)
    }

    // ✅ UI lives OUTSIDE remember
    Box(modifier = modifier.fillMaxSize()) {

        CameraPermissionGate {
            FaceCameraHost(
                useFrontCamera = !useBackCamera,
                analyzer = analyzer, // ImageAnalysis.Analyzer ✅
                modifier = Modifier.fillMaxSize()
            )
        }

        FaceOverlay(
            faceBounds = controller.faceBounds,
            imageSize = controller.imageSize,
            imageRotation = controller.imageRotation,
            isFrontCamera = !useBackCamera,
            modifier = Modifier.fillMaxSize(),
            paddingFactor = 0.1f
        )
    }
}
