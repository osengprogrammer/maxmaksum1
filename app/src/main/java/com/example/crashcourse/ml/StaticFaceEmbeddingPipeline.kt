package com.example.crashcourse.ml

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * StaticFaceEmbeddingPipeline
 *
 * For static images:
 * - bulk registration
 * - gallery upload
 * - single photo registration
 *
 * IMPORTANT:
 * ❌ No android.media.Image
 * ❌ No BitmapImageAdapter
 * ✅ NV21-only native preprocessing
 */
object StaticFaceEmbeddingPipeline {

    private const val TAG = "StaticFacePipeline"

    enum class FacePolicy {
        SINGLE_ONLY,
        LARGEST_FACE
    }

    suspend fun extract(
        bitmap: Bitmap,
        policy: FacePolicy = FacePolicy.SINGLE_ONLY
    ): Pair<Bitmap, FloatArray>? = withContext(Dispatchers.IO) {

        try {
            Log.e(TAG, "ENTER extract()")

            // --------------------------------------------------
            // 1. Face detection (ML Kit on Bitmap)
            // --------------------------------------------------
            val faces = detectFaces(bitmap)
            Log.e(TAG, "Faces detected: ${faces.size}")

            if (faces.isEmpty()) {
                Log.e(TAG, "EXIT: No faces detected")
                return@withContext null
            }

            // --------------------------------------------------
            // 2. Select face
            // --------------------------------------------------
            val selectedFace = when (policy) {
                FacePolicy.SINGLE_ONLY ->
                    if (faces.size == 1) faces.first() else null

                FacePolicy.LARGEST_FACE ->
                    faces.maxByOrNull {
                        it.boundingBox.width() * it.boundingBox.height()
                    }
            }

            if (selectedFace == null) {
                Log.e(TAG, "EXIT: Face selection failed (policy=$policy)")
                return@withContext null
            }

            val faceRect: Rect = selectedFace.boundingBox
            Log.e(TAG, "Selected face rect: $faceRect")

            // --------------------------------------------------
            // 3. Crop face bitmap (for saving / preview)
            // --------------------------------------------------
            val faceBitmap = cropFace(bitmap, faceRect)
            Log.e(
                TAG,
                "Cropped face bitmap: ${faceBitmap.width}x${faceBitmap.height}"
            )

            // --------------------------------------------------
            // 4. Bitmap → NV21
            // --------------------------------------------------
            val nv21 = BitmapNV21Converter.fromBitmap(bitmap)
            Log.e(TAG, "NV21 generated (${nv21.size} bytes)")

            // --------------------------------------------------
            // 5. Native preprocessing (NV21)
            // --------------------------------------------------
            Log.e(TAG, "Calling native preprocess (NV21)")

            val buffer = BitmapUtils.preprocessFaceNV21(
                nv21 = nv21,
                width = bitmap.width,
                height = bitmap.height,
                left = faceRect.left,
                top = faceRect.top,
                right = faceRect.right,
                bottom = faceRect.bottom,
                rotation = 0
            )

            Log.e(TAG, "Native preprocess OK")

            // --------------------------------------------------
            // 6. Embedding
            // --------------------------------------------------
            val embedding = FaceRecognizer.recognizeFace(buffer)

            Log.e(
                TAG,
                "STATIC embedding sample: ${
                    embedding.take(10).joinToString(
                        prefix = "[",
                        postfix = "]"
                    ) { String.format("%.4f", it) }
                }"
            )

            val minVal = embedding.minOrNull()
            val maxVal = embedding.maxOrNull()
            val avgVal = embedding.average()

            Log.e(
                TAG,
                "STATIC stats → min=$minVal max=$maxVal avg=$avgVal"
            )

            Pair(faceBitmap, embedding)

        } catch (e: Exception) {
            Log.e(TAG, "FAILED: ${e.message}", e)
            null
        }
    }

    // --------------------------------------------------
    // Face detection helper
    // --------------------------------------------------
    private suspend fun detectFaces(bitmap: Bitmap): List<Face> =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)

            val detector = FaceDetection.getClient(
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build()
            )

            detector.process(image)
                .addOnSuccessListener { faces ->
                    cont.resume(faces)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection FAILED", e)
                    cont.resume(emptyList())
                }
        }

    // --------------------------------------------------
    // Safe crop helper
    // --------------------------------------------------
    private fun cropFace(bitmap: Bitmap, rect: Rect): Bitmap {
        val left = rect.left.coerceAtLeast(0)
        val top = rect.top.coerceAtLeast(0)
        val right = rect.right.coerceAtMost(bitmap.width)
        val bottom = rect.bottom.coerceAtMost(bitmap.height)

        return Bitmap.createBitmap(
            bitmap,
            left,
            top,
            right - left,
            bottom - top
        )
    }
}
