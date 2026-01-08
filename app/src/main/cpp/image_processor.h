#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>

#define FACE_INPUT_SIZE 160
#define FACE_CHANNELS 3

/**
 * Native face preprocessing.
 *
 * Input:
 *  - nv21       : camera frame in NV21 format
 *  - width      : image width
 *  - height     : image height
 *  - left/top/right/bottom : face bounding box
 *  - rotation   : rotation degrees (0, 90, 180, 270)
 *
 * Output:
 *  - outBuffer  : float buffer (160 * 160 * 3), normalized to [-1, 1]
 */
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
);

#ifdef __cplusplus
}
#endif

#endif // IMAGE_PROCESSOR_H
