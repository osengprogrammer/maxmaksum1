package com.example.crashcourse.domain.config

/**
 * Default biometric policy for Azura systems.
 *
 * Values are centralized and derived from
 * FaceRecognitionConfig to ensure consistency.
 */
class DefaultFaceMatchPolicy : FaceMatchPolicy {

    /**
     * Registration must be strict.
     * Reuse MAX_DISTANCE to ensure consistency
     * with core biometric expectations.
     */
    override fun registrationThreshold(): Float =
        FaceRecognitionConfig.MAX_DISTANCE

    /**
     * Recognition can be slightly more tolerant.
     * This value can later be tuned or A/B tested.
     */
    override fun recognitionThreshold(): Float =
        0.40f

    /**
     * Minimum confidence gap to accept a match.
     * Helps avoid ambiguous matches.
     */
    override fun minMargin(): Float =
        FaceRecognitionConfig.MIN_MARGIN
}
