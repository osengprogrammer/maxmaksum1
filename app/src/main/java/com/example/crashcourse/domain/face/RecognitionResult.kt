package com.example.crashcourse.domain.face

sealed class RecognitionResult {

    data class Match(
        val id: String,
        val distance: Float
    ) : RecognitionResult()

    data class NoMatch(
        val reason: String
    ) : RecognitionResult()
}
