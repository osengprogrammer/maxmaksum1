package com.example.crashcourse.domain

import com.example.crashcourse.data.cache.FaceCacheManager
import com.example.crashcourse.data.repository.FaceRepository
import com.example.crashcourse.db.FaceEntity

class RegisterFaceUseCase(
    private val cacheManager: FaceCacheManager,
    private val faceRepository: FaceRepository,
    private val duplicateDetector: FaceDuplicateDetector
) {

    suspend fun execute(
        studentId: String,
        name: String,
        embedding: FloatArray,
        photoUrl: String?,
        className: String,
        subClass: String,
        grade: String,
        subGrade: String,
        program: String,
        role: String
    ): RegisterFaceResult {

        val existing = faceRepository.getByStudentId(studentId)
        if (existing != null) {
            return RegisterFaceResult.DuplicateStudentId(existing.name)
        }

        val cache = cacheManager.load()
        val duplicateName = duplicateDetector.findDuplicate(cache, embedding)
        if (duplicateName != null) {
            return RegisterFaceResult.DuplicateFace(duplicateName)
        }

        val face = FaceEntity(
            studentId = studentId,
            name = name,
            photoUrl = photoUrl,
            embedding = embedding,
            className = className,
            subClass = subClass,
            grade = grade,
            subGrade = subGrade,
            program = program,
            role = role,
            timestamp = System.currentTimeMillis()
        )

        faceRepository.insert(face)
        cacheManager.refresh()

        return RegisterFaceResult.Success
    }
}
