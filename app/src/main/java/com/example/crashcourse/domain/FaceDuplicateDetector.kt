package com.example.crashcourse.domain

import com.example.crashcourse.domain.config.FaceMatchPolicy
import com.example.crashcourse.domain.face.FaceMatcher
import com.example.crashcourse.domain.face.RecognitionResult

/**
 * Registration-time duplicate guard.
 *
 * Delegates biometric truth to FaceMatcher
 * and applies registration-specific policy.
 */
class FaceDuplicateDetector(
    private val matcher: FaceMatcher,
    private val policy: FaceMatchPolicy
) {

    fun findDuplicate(
        cache: Map<String, FloatArray>,
        embedding: FloatArray
    ): String? {

        val result = matcher.matchBest(
            gallery = cache,
            probe = embedding,
            threshold = policy.registrationThreshold(),
            minMargin = policy.minMargin()   // ✅ NEW (important)
        )

        return when (result) {
            is RecognitionResult.Match -> result.id
            is RecognitionResult.NoMatch -> null
        }
    }
}
