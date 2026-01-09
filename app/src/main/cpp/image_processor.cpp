#include "image_processor.h"
#include <android/log.h>
#include <math.h>

#define LOG_TAG "ImageProcessor"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define INPUT_SIZE 160

static inline int clamp(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
}

// rotation mapping
static inline void rotatePoint(
    int x, int y,
    int width, int height,
    int rotation,
    int* rx, int* ry
) {
    switch (rotation) {
        case 90:
            *rx = y;
            *ry = width - 1 - x;
            break;
        case 180:
            *rx = width - 1 - x;
            *ry = height - 1 - y;
            break;
        case 270:
            *rx = height - 1 - y;
            *ry = x;
            break;
        default:
            *rx = x;
            *ry = y;
            break;
    }
}

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
    if (!nv21 || !outBuffer) return;

    left   = clamp(left,   0, width  - 1);
    right  = clamp(right,  0, width);
    top    = clamp(top,    0, height - 1);
    bottom = clamp(bottom, 0, height);

    int fw = right - left;
    int fh = bottom - top;
    if (fw <= 0 || fh <= 0) return;

    // ---- force square ----
    int size = fw > fh ? fw : fh;
    int cx = left + fw / 2;
    int cy = top  + fh / 2;

    left = clamp(cx - size / 2, 0, width  - size);
    top  = clamp(cy - size / 2, 0, height - size);

    float scale = (float)size / INPUT_SIZE;
    int frameSize = width * height;

    int outIdx = 0;

    for (int y = 0; y < INPUT_SIZE; y++) {
        float srcYf = top + (y + 0.5f) * scale;
        int srcY = clamp((int)floorf(srcYf), 0, height - 1);

        for (int x = 0; x < INPUT_SIZE; x++) {
            float srcXf = left + (x + 0.5f) * scale;
            int srcX = clamp((int)floorf(srcXf), 0, width - 1);

            int rx, ry;
            rotatePoint(srcX, srcY, width, height, rotation, &rx, &ry);

            rx = clamp(rx, 0, width  - 1);
            ry = clamp(ry, 0, height - 1);

            int yIdx = ry * width + rx;
            int uvIdx = frameSize + (ry / 2) * width + (rx / 2) * 2;

            uint8_t Y = nv21[yIdx];
            uint8_t V = nv21[uvIdx];
            uint8_t U = nv21[uvIdx + 1];

            int C = (int)Y - 16;
            int D = (int)U - 128;
            int E = (int)V - 128;

            int R = (298 * C + 409 * E + 128) >> 8;
            int G = (298 * C - 100 * D - 208 * E + 128) >> 8;
            int B = (298 * C + 516 * D + 128) >> 8;

            R = clamp(R, 0, 255);
            G = clamp(G, 0, 255);
            B = clamp(B, 0, 255);

            outBuffer[outIdx++] = (R - 127.5f) / 127.5f;
            outBuffer[outIdx++] = (G - 127.5f) / 127.5f;
            outBuffer[outIdx++] = (B - 127.5f) / 127.5f;
        }
    }
}
