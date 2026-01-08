#include <jni.h>
#include <android/log.h>
#include <vector>
#include "image_processor.h"

#define LOG_TAG "FacePreprocessJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

/**
 * ByteBuffer preprocessFace(
 *   byte[] nv21,
 *   int width,
 *   int height,
 *   int left,
 *   int top,
 *   int right,
 *   int bottom,
 *   int rotation
 * )
 */
JNIEXPORT jobject JNICALL
Java_com_example_crashcourse_nativebridge_FaceBridge_preprocessFace(
    JNIEnv* env,
    jclass,
    jbyteArray nv21,
    jint width,
    jint height,
    jint left,
    jint top,
    jint right,
    jint bottom,
    jint rotation
) {
    if (!nv21) {
        LOGE("nv21 input is null");
        return nullptr;
    }

    const int outputSize =
        FACE_INPUT_SIZE * FACE_INPUT_SIZE * FACE_CHANNELS;

    // Allocate native float buffer
    float* outputBuffer = new float[outputSize];

    jbyte* nv21Data = env->GetByteArrayElements(nv21, nullptr);
    if (!nv21Data) {
        LOGE("Failed to get NV21 data");
        delete[] outputBuffer;
        return nullptr;
    }

    preprocessImage(
        reinterpret_cast<uint8_t*>(nv21Data),
        width,
        height,
        left,
        top,
        right,
        bottom,
        rotation,
        outputBuffer
    );

    env->ReleaseByteArrayElements(nv21, nv21Data, JNI_ABORT);

    // Wrap float buffer into DirectByteBuffer
    jobject byteBuffer = env->NewDirectByteBuffer(
        outputBuffer,
        outputSize * sizeof(float)
    );

    // IMPORTANT:
    // Ownership of outputBuffer is now with JVM.
    // It will be freed when ByteBuffer is GC’d.

    LOGD("Preprocessing finished, returning ByteBuffer");
    return byteBuffer;
}

} // extern "C"
