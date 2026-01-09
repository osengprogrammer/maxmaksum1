package com.example.crashcourse.domain.config

object FaceRecognitionConfig {

    // cosine distance threshold (lower = stricter)
    const val MAX_DISTANCE = 0.25f

    // minimum gap between best and second-best
    const val MIN_MARGIN = 0.05f

    // embedding contract
    const val EMBEDDING_SIZE = 512
}
