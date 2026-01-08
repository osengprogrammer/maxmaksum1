package com.example.crashcourse.domain

import android.graphics.Bitmap
import com.example.crashcourse.data.cache.FaceCacheManager
import com.example.crashcourse.data.repository.FacePhotoRepository
import com.example.crashcourse.data.repository.FaceRepository
import com.example.crashcourse.db.FaceEntity

class UpdateFaceUseCase(
    private val faceRepository: FaceRepository,
    private val photoRepository: FacePhotoRepository,
    private val cacheManager: FaceCacheManager
) {

    suspend fun execute(
        face: FaceEntity,
        photo: Bitmap?,
        embedding: FloatArray
    ): UpdateFaceResult {
        return try {
            val photoUrl = if (photo != null) {
                photoRepository.savePhoto(photo, face.studentId)
            } else {
                face.photoUrl
            }

            if (photoUrl != null && photoUrl != face.photoUrl) {
                photoRepository.cleanupOldPhotos(face.studentId, photoUrl)
            }

            val updatedFace = face.copy(
                photoUrl = photoUrl,
                embedding = embedding
            )

            faceRepository.update(updatedFace)
            cacheManager.refresh()

            UpdateFaceResult.Success
        } catch (e: Exception) {
            UpdateFaceResult.Error(e.message ?: "Failed to update face")
        }
    }
}
