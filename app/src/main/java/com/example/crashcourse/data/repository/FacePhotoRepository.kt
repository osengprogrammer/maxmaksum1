package com.example.crashcourse.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.crashcourse.utils.PhotoStorageUtils

class FacePhotoRepository(
    private val context: Context
) {

    fun savePhoto(bitmap: Bitmap, studentId: String): String? {
        return PhotoStorageUtils.saveFacePhoto(context, bitmap, studentId)
    }

    fun cleanupOldPhotos(studentId: String, keepPath: String) {
        PhotoStorageUtils.cleanupOldPhotos(context, studentId, keepPath)
    }
}
