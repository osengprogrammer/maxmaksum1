#include "image_processor.h"
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Must match your TFLite model input size
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
    // 1. Safety Checks
    if (!nv21 || !outBuffer) {
        LOGE("Null input or output buffer");
        return;
    }

    if (width <= 0 || height <= 0) {
        LOGE("Invalid image size: %dx%d", width, height);
        return;
    }

    // 2. Validate and Clamp Bounding Box
    // Ensure the box is inside the image logic
    if (left < 0) left = 0;
    if (top < 0) top = 0;
    if (right > width) right = width;
    if (bottom > height) bottom = height;

    int faceW = right - left;
    int faceH = bottom - top;

    if (faceW <= 0 || faceH <= 0) {
        LOGE("Invalid face dimensions after clamping: %dx%d", faceW, faceH);
        return; // Box is invalid or outside image
    }

    // 3. Scaling Factors
    // We want to fill a 160x160 buffer. We map the target (x,y) back to the source image.
    // This is a "Nearest Neighbor" resize implementation.
    float scaleX = (float)faceW / INPUT_SIZE;
    float scaleY = (float)faceH / INPUT_SIZE;

    int outIndex = 0;

    // 4. Loop through target 160x160 pixels
    for (int y = 0; y < INPUT_SIZE; y++) {
        // Find corresponding Y row in the source image
        int srcY = top + (int)(y * scaleY);
        if (srcY >= height) srcY = height - 1;

        for (int x = 0; x < INPUT_SIZE; x++) {
            // Find corresponding X col in the source image
            int srcX = left + (int)(x * scaleX);
            if (srcX >= width) srcX = width - 1;

            // 5. NV21 / YUV Extraction
            // Y plane is the first w * h bytes
            int yIdx = srcY * width + srcX;

            // UV plane starts after Y.
            // NV21 layout: YYYYYYYY... VUVUVU...
            // V and U are subsampled by 2x2.
            // (srcY / 2) * width gives the row in the UV plane.
            // (srcX / 2) * 2 gives the column pair.
            int uvIdx = width * height + (srcY / 2) * width + (srcX / 2) * 2;

            uint8_t Y_val = nv21[yIdx];
            uint8_t V_val = nv21[uvIdx];     // NV21: V is first
            uint8_t U_val = nv21[uvIdx + 1]; // NV21: U is second

            // 6. YUV to RGB Conversion (Standard Formula)
            // Using integer math for speed
            int C = Y_val - 16;
            int D = U_val - 128;
            int E = V_val - 128;

            int R = (298 * C + 409 * E + 128) >> 8;
            int G = (298 * C - 100 * D - 208 * E + 128) >> 8;
            int B = (298 * C + 516 * D + 128) >> 8;

            // Clamp to [0, 255]
            if (R < 0) R = 0; else if (R > 255) R = 255;
            if (G < 0) G = 0; else if (G > 255) G = 255;
            if (B < 0) B = 0; else if (B > 255) B = 255;

            // 7. Normalize to [-1, 1] for FaceNet
            // Formula: (pixel - 127.5) / 127.5
            outBuffer[outIndex++] = (R - 127.5f) / 127.5f;
            outBuffer[outIndex++] = (G - 127.5f) / 127.5f;
            outBuffer[outIndex++] = (B - 127.5f) / 127.5f;
        }
    }
}