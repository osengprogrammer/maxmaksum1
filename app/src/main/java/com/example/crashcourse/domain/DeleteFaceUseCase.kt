package com.example.crashcourse.domain

import com.example.crashcourse.data.cache.FaceCacheManager
import com.example.crashcourse.data.repository.FaceRepository
import com.example.crashcourse.db.FaceEntity

class DeleteFaceUseCase(
    private val repository: FaceRepository,
    private val cacheManager: FaceCacheManager
) {

    suspend fun execute(face: FaceEntity) {
        repository.delete(face)
        cacheManager.refresh()
    }
}
