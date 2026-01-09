package com.example.crashcourse.domain.face

import com.example.crashcourse.domain.config.FaceRecognitionConfig
import com.example.crashcourse.utils.cosineDistance
import kotlin.math.sqrt

/**
 * FaceMatcher is the SINGLE source of biometric truth.
 */
class FaceMatcher(
    private val logger: BiometricDecisionLogger? = null
) {

    fun matchBest(
        gallery: Map<String, FloatArray>,
        probe: FloatArray,
        threshold: Float,
        minMargin: Float = 0f
    ): RecognitionResult {

        require(probe.size == FaceRecognitionConfig.EMBEDDING_SIZE) {
            "Probe embedding size mismatch"
        }

        if (gallery.isEmpty()) {
            return RecognitionResult.NoMatch("Gallery is empty")
        }

        val normProbe = l2Normalize(probe)

        var bestId: String? = null
        var bestDistance = Float.MAX_VALUE
        var secondBestDistance = Float.MAX_VALUE

        for ((id, emb) in gallery) {
            require(emb.size == FaceRecognitionConfig.EMBEDDING_SIZE) {
                "Gallery embedding size mismatch for id=$id"
            }

            val distance = cosineDistance(
                l2Normalize(emb),
                normProbe
            )

            if (distance < bestDistance) {
                secondBestDistance = bestDistance
                bestDistance = distance
                bestId = id
            } else if (distance < secondBestDistance) {
                secondBestDistance = distance
            }
        }

        // Threshold check
        if (bestId == null || bestDistance > threshold) {
            return RecognitionResult.NoMatch("No candidate under threshold")
        }

        // 🔍 Ambiguity (margin) check
        if (
            minMargin > 0f &&
            secondBestDistance < Float.MAX_VALUE &&
            (secondBestDistance - bestDistance) < minMargin
        ) {

            // ✅ LOG ONLY HERE (important)
            logger?.onAmbiguousMatch(
                bestId = bestId,
                bestDistance = bestDistance,
                secondBestDistance = secondBestDistance,
                threshold = threshold,
                minMargin = minMargin
            )

            return RecognitionResult.NoMatch("Ambiguous match (margin too small)")
        }

        return RecognitionResult.Match(
            id = bestId,
            distance = bestDistance
        )
    }

    private fun l2Normalize(v: FloatArray): FloatArray {
        var sum = 0f
        for (x in v) sum += x * x
        val norm = sqrt(sum)
        if (norm == 0f) return v
        return FloatArray(v.size) { i -> v[i] / norm }
    }
}
