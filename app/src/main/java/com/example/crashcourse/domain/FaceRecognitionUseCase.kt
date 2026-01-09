package com.example.crashcourse.domain

import com.example.crashcourse.domain.config.FaceMatchPolicy
import com.example.crashcourse.domain.face.FaceMatcher
import com.example.crashcourse.domain.face.RecognitionResult

/**
 * Convenience recognition use case.
 *
 * Thin adapter over FaceMatcher.
 */
class FaceRecognitionUseCase(
    private val matcher: FaceMatcher,
    private val policy: FaceMatchPolicy
) {

    fun findBestMatch(
        gallery: List<Pair<String, FloatArray>>,
        embedding: FloatArray
    ): String? {

        if (gallery.isEmpty()) return null

        val galleryMap = gallery.toMap()

        val result = matcher.matchBest(
            gallery = galleryMap,
            probe = embedding,
            threshold = policy.recognitionThreshold(),
            minMargin = policy.minMargin()   // ✅ NEW (important)
        )

        return when (result) {
            is RecognitionResult.Match -> result.id
            is RecognitionResult.NoMatch -> null
        }
    }
}
