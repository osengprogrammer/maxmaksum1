package com.example.crashcourse.scanner

import android.graphics.Rect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize

class FaceAnalyzerController(
    val policy: MultiFacePolicy = MultiFacePolicy.SINGLE_ONLY
) {
    var faceBounds by mutableStateOf<List<Rect>>(emptyList())
        private set

    var imageSize by mutableStateOf(IntSize.Zero)
        private set

    var imageRotation by mutableStateOf(0)
        private set

    var embedding by mutableStateOf<FloatArray?>(null)
        private set

    val hasFace: Boolean get() = faceBounds.isNotEmpty()
    val hasMultipleFaces: Boolean get() = faceBounds.size > 1

    /**
     * Called with lists of faces & embeddings from analyzer.
     * Even if you only detect one face today, pass lists forward.
     */
    fun onFacesDetected(
        faces: List<Rect>,
        embeddings: List<FloatArray>,
        imageSize: IntSize,
        rotation: Int
    ) {
        this.imageSize = imageSize
        this.imageRotation = rotation

        if (faces.isEmpty()) {
            resetFaces()
            return
        }

        when (policy) {
            MultiFacePolicy.SINGLE_ONLY -> {
                if (faces.size == 1) {
                    faceBounds = faces
                    embedding = embeddings.firstOrNull()
                } else {
                    faceBounds = faces
                    embedding = null // reject multi-face for SINGLE_ONLY
                }
            }

            MultiFacePolicy.LARGEST_FACE -> {
                val index = faces.indices.maxByOrNull { faces[it].width() * faces[it].height() } ?: 0
                faceBounds = listOf(faces[index])
                embedding = embeddings.getOrNull(index)
            }

            MultiFacePolicy.FIRST_DETECTED -> {
                faceBounds = listOf(faces.first())
                embedding = embeddings.firstOrNull()
            }
        }
    }

    private fun resetFaces() {
        faceBounds = emptyList()
        embedding = null
    }

    fun resetAll() {
        resetFaces()
        imageSize = IntSize.Zero
        imageRotation = 0
    }
}
