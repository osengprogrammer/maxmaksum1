package com.example.crashcourse.data.cache

import android.content.Context
import com.example.crashcourse.db.FaceCache

class FaceCacheManager(
    private val context: Context
) {

    suspend fun refresh() {
        FaceCache.refresh(context)
    }

    suspend fun load(): Map<String, FloatArray> {
        return FaceCache.load(context).toMap()
    }
}
