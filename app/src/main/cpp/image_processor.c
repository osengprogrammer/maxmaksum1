#include "image_processor.h"
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define INPUT_SIZE 160

void preprocessImage(
    const uint8_t* nv21,
    int width,
    int height,
    int left,
    int top,
    int right,
    int bottom,
    int rotation,
    float* outBuffer
) {
    if (!nv21 || !outBuffer) {
        LOGE("Null input or output buffer");
        return;
    }

    if (width <= 0 || height <= 0) {
        LOGE("Invalid image size");
        return;
    }

    // Clamp bounding box
    if (left < 0) left = 0;
    if (top < 0) top = 0;
    if (right > width) right = width;
    if (bottom > height) bottom = height;

    int faceW = right - left;
    int faceH = bottom - top;

    if (faceW <= 0 || faceH <= 0) {
        LOGE("Invalid face rect");
        return;
    }

    LOGI("Preprocess start: %dx%d → face %dx%d",
         width, height, faceW, faceH);

    /*
     * NOTE:
     * Here is where you implement:
     * 1. NV21 → RGB
     * 2. Crop face
     * 3. Resize to 160x160
     * 4. Normalize to [-1, 1]
     *
     * For now, we only zero-fill output (safe placeholder)
     */

    memset(outBuffer, 0, INPUT_SIZE * INPUT_SIZE * 3 * sizeof(float));

    LOGI("Preprocess finished (placeholder)");
}
