package com.example.crashcourse.data

import android.content.Context
import android.graphics.Bitmap
import com.example.crashcourse.utils.PhotoStorageUtils

class PhotoRepository {

    fun saveUserPhoto(
        context: Context,
        bitmap: Bitmap,
        studentId: String
    ): String? {
        return try {
            PhotoStorageUtils.saveFacePhoto(context, bitmap, studentId)
        } catch (e: Exception) {
            null
        }
    }
}
