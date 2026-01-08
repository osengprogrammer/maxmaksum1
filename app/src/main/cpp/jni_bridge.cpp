#include <jni.h>
#include <android/log.h>
#include <cstdlib>
#include "image_processor.h"

#define LOG_TAG "FacePreprocessJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

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

    float* outputBuffer =
        (float*) malloc(outputSize * sizeof(float));

    if (!outputBuffer) {
        LOGE("Failed to allocate output buffer");
        return nullptr;
    }

    jbyte* nv21Data = env->GetByteArrayElements(nv21, nullptr);
    if (!nv21Data) {
        LOGE("Failed to get NV21 data");
        free(outputBuffer);
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

    jobject byteBuffer = env->NewDirectByteBuffer(
        outputBuffer,
        outputSize * sizeof(float)
    );

    if (!byteBuffer) {
        LOGE("Failed to create DirectByteBuffer");
        free(outputBuffer);
        return nullptr;
    }

    LOGD("Preprocessing finished, returning ByteBuffer");
    return byteBuffer;

    // NOTE:
    // outputBuffer will be freed later when lifecycle control is added.
}

} // extern "C"
