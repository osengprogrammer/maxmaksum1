package com.example.crashcourse.ml

import android.graphics.Rect
import android.media.Image
import com.example.crashcourse.nativebridge.FaceBridge
import java.nio.ByteBuffer

/**
 * BitmapUtils
 *
 * Responsibility:
 * - Convert image sources to NV21
 * - Delegate ALL preprocessing to native layer
 *
 * DOES NOT:
 * - Resize
 * - Normalize
 * - Rotate
 * - Crop
 * - Run ML inference
 */
object BitmapUtils {

    // =====================================================
    // CAMERA PIPELINE (Image → NV21 → Native)
    // =====================================================

    /**
     * Camera entry point
     *
     * Used by:
     * - FaceAnalyzer (real-time camera)
     *
     * @return Direct ByteBuffer (Float32, 160x160x3)
     */
    fun preprocessFace(
        image: Image,
        boundingBox: Rect,
        rotation: Int
    ): ByteBuffer {
        val nv21 = imageToNV21(image)

        return FaceBridge.preprocessFace(
            nv21 = nv21,
            width = image.width,
            height = image.height,
            left = boundingBox.left,
            top = boundingBox.top,
            right = boundingBox.right,
            bottom = boundingBox.bottom,
            rotation = rotation
        )
    }

    /**
     * Convert Camera Image (YUV_420_888) → NV21
     *
     * This is the ONLY image logic kept in Kotlin for camera.
     */
    private fun imageToNV21(image: Image): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // Y
        yPlane.buffer.get(nv21, 0, ySize)

        // NV21 format = Y + V + U
        vPlane.buffer.get(nv21, ySize, vSize)
        uPlane.buffer.get(nv21, ySize + vSize, uSize)

        return nv21
    }

    // =====================================================
    // STATIC PIPELINE (NV21 → Native)
    // =====================================================

    /**
     * Static image entry point (gallery / bulk / single upload)
     *
     * Used by:
     * - StaticFaceEmbeddingPipeline
     *
     * @return Direct ByteBuffer (Float32, 160x160x3)
     */
    fun preprocessFaceNV21(
        nv21: ByteArray,
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        rotation: Int
    ): ByteBuffer {
        return FaceBridge.preprocessFace(
            nv21 = nv21,
            width = width,
            height = height,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            rotation = rotation
        )
    }
}
