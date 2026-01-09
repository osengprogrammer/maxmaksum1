package com.example.crashcourse.domain.config

/**
 * Central contract for biometric matching rules.
 *
 * This interface defines HOW strict biometric decisions are,
 * without exposing magic numbers to ViewModels or UseCases.
 */
interface FaceMatchPolicy {

    /**
     * Threshold used when registering a new face.
     * Must be strict to avoid duplicate identities.
     */
    fun registrationThreshold(): Float

    /**
     * Threshold used during recognition (attendance / auth).
     * Can be more tolerant to pose, lighting, aging.
     */
    fun recognitionThreshold(): Float

    /**
     * Minimum distance margin between best and second-best match.
     * Used to reduce false positives.
     */
    fun minMargin(): Float
}
