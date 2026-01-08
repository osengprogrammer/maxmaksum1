package com.example.crashcourse.domain

import com.example.crashcourse.utils.cosineDistance

class FaceDuplicateDetector(
    private val threshold: Float
) {

    fun findDuplicate(
        cache: Map<String, FloatArray>,
        embedding: FloatArray
    ): String? {

        var bestName: String? = null
        var bestDistance = Float.MAX_VALUE

        for ((name, existingEmbedding) in cache) {
            val distance = cosineDistance(existingEmbedding, embedding)
            if (distance < bestDistance) {
                bestDistance = distance
                bestName = name
            }
        }

        return if (bestName != null && bestDistance <= threshold) {
            bestName
        } else {
            null
        }
    }
}
