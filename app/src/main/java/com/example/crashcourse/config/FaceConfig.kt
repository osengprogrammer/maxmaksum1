package com.example.crashcourse.config

object FaceConfig {

    /**
     * Cosine distance threshold.
     * GPU inference → stricter threshold.
     */
    const val RECOGNITION_THRESHOLD = 0.25f

    /**
     * MUST match model output.
     * Netron shows: float32[1,512]
     */
    const val EMBEDDING_SIZE = 512
}
