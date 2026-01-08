package com.example.crashcourse.ml

import android.graphics.Rect
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.ui.unit.IntSize
import com.example.crashcourse.scanner.FaceAnalyzerController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("UnsafeOptInUsageError")
class FaceAnalyzer(
    private val controller: FaceAnalyzerController
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()
    )

    // Concurrency + FPS throttle
    private val isProcessing = AtomicBoolean(false)
    private var lastProcessTime = 0L
    private val throttleMs = 100L // ~10 FPS

    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()

        if (isProcessing.get() || now - lastProcessTime < throttleMs) {
            imageProxy.close()
            return
        }

        isProcessing.set(true)
        lastProcessTime = now

        val mediaImage: Image? = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            isProcessing.set(false)
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees

        val imageSize = if (rotation % 180 == 0) {
            IntSize(imageProxy.width, imageProxy.height)
        } else {
            IntSize(imageProxy.height, imageProxy.width)
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                try {
                    if (faces.isEmpty()) {
                        controller.onFacesDetected(
                            faces = emptyList(),
                            embeddings = emptyList(),
                            imageSize = imageSize,
                            rotation = rotation
                        )
                        return@addOnSuccessListener
                    }

                    val rects: List<Rect> = faces.map { it.boundingBox }
                    val embeddings = mutableListOf<FloatArray>()

                    for (face in faces) {
                        try {
                            val buffer = BitmapUtils.preprocessFace(
                                image = mediaImage,
                                boundingBox = face.boundingBox,
                                rotation = rotation
                            )

                            val embedding = FaceRecognizer.recognizeFace(buffer)
                            embeddings.add(embedding)
                            Log.e(
    "FaceAnalyzer",
    "CAMERA embedding sample: ${
        embedding.take(10).joinToString(
            prefix = "[",
            postfix = "]"
        ) { String.format("%.4f", it) }
    }"
)

val min = embedding.minOrNull()
val max = embedding.maxOrNull()
val avg = embedding.average()

Log.e(
    "FaceAnalyzer",
    "CAMERA stats → min=$min max=$max avg=$avg"
)
Log.e(
    "FaceAnalyzer",
    "CAMERA embedding sample: ${
        embedding.take(10).joinToString(
            prefix = "[",
            postfix = "]"
        ) { String.format("%.4f", it) }
    }"
)

val camMin = embedding.minOrNull()
val camMax = embedding.maxOrNull()
val camAvg = embedding.average()

Log.e(
    "FaceAnalyzer",
    "CAMERA stats → min=$min max=$max avg=$avg"
)


                        } catch (e: Exception) {
                            Log.e(
                                "FaceAnalyzer",
                                "Embedding failed for one face",
                                e
                            )
                            embeddings.add(FloatArray(0)) // keep index alignment
                        }
                    }

                    controller.onFacesDetected(
                        faces = rects,
                        embeddings = embeddings,
                        imageSize = imageSize,
                        rotation = rotation
                    )

                    Log.d(
                        "FaceAnalyzer",
                        "Detected ${faces.size} face(s)"
                    )

                } catch (e: Exception) {
                    Log.e(
                        "FaceAnalyzer",
                        "Error handling detection result",
                        e
                    )

                    controller.onFacesDetected(
                        faces = emptyList(),
                        embeddings = emptyList(),
                        imageSize = imageSize,
                        rotation = rotation
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("FaceAnalyzer", "Face detection failed", e)

                controller.onFacesDetected(
                    faces = emptyList(),
                    embeddings = emptyList(),
                    imageSize = imageSize,
                    rotation = rotation
                )
            }
            .addOnCompleteListener {
                try {
                    imageProxy.close()
                } catch (_: Exception) {
                }
                isProcessing.set(false)
            }
    }

    fun close() {
        detector.close()
    }
}
