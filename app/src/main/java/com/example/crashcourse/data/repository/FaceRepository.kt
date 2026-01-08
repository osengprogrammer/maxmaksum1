package com.example.crashcourse.data.repository

import com.example.crashcourse.db.FaceDao
import com.example.crashcourse.db.FaceEntity

class FaceRepository(
    private val faceDao: FaceDao
) {

    suspend fun insert(face: FaceEntity) {
        faceDao.insert(face)
    }

    suspend fun update(face: FaceEntity) {
        faceDao.update(face)
    }

    suspend fun delete(face: FaceEntity) {
        faceDao.delete(face)
    }

    suspend fun getByStudentId(studentId: String): FaceEntity? {
        return faceDao.getFaceByStudentId(studentId)
    }
}
