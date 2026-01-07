package com.example.crashcourse.domain

import com.example.crashcourse.utils.cosineDistance

class FaceRecognitionUseCase(
    private val threshold: Float = 0.4f
) {

    fun findBestMatch(
        gallery: List<Pair<String, FloatArray>>,
        embedding: FloatArray
    ): String? {
        val best = gallery.minByOrNull {
            cosineDistance(it.second, embedding)
        } ?: return null

        return if (cosineDistance(best.second, embedding) < threshold) {
            best.first
        } else {
            null
        }
    }
}
