package com.example.crashcourse.ml

import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 * Bitmap → NV21 converter
 *
 * Used ONLY for static images (gallery / bulk upload).
 * Camera images already come as NV21/YUV.
 */
object BitmapNV21Converter {

    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        val nv21 = ByteArray(width * height * 3 / 2)

        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val color = argb[j * width + i]

                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF

                // YUV conversion
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                nv21[yIndex++] = y.coerceIn(0, 255).toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    nv21[uvIndex++] = v.coerceIn(0, 255).toByte()
                    nv21[uvIndex++] = u.coerceIn(0, 255).toByte()
                }
            }
        }

        return nv21
    }
}
