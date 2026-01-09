package com.example.crashcourse.domain.face

/**
 * Optional logger for biometric decisions.
 * Used for tuning and debugging false positives.
 */
interface BiometricDecisionLogger {

    fun onAmbiguousMatch(
        bestId: String,
        bestDistance: Float,
        secondBestDistance: Float,
        threshold: Float,
        minMargin: Float
    )
}
