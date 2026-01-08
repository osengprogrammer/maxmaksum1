package com.example.crashcourse.nativebridge

import java.nio.ByteBuffer

object FaceBridge {

    init {
        System.loadLibrary("azura_face_lib")
    }

    /**
     * Native face preprocessing.
     *
     * Input  : NV21 camera frame + face bounding box
     * Output : Direct ByteBuffer (Float32, 160x160x3, normalized [-1,1])
     */
    external fun preprocessFace(
        nv21: ByteArray,
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        rotation: Int
    ): ByteBuffer
}
