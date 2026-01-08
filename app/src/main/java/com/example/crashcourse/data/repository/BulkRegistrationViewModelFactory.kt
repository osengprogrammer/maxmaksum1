package com.example.crashcourse.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.crashcourse.data.cache.FaceCacheManager
import com.example.crashcourse.data.repository.FaceRepository
import com.example.crashcourse.db.AppDatabase
import com.example.crashcourse.domain.*

class BulkRegistrationViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BulkRegistrationViewModel::class.java)) {

            // ---- DB ----
            val database = AppDatabase.getInstance(application)

            // ---- Repositories ----
            val faceRepository = FaceRepository(database.faceDao())
            val cacheManager = FaceCacheManager(application)

            // ---- UseCases ----
            val registerFaceUseCase = RegisterFaceUseCase(
                cacheManager = cacheManager,
                faceRepository = faceRepository,
                duplicateDetector = FaceDuplicateDetector(threshold = 0.3f)
            )

            val bulkRegisterUseCase = BulkRegisterUseCase(registerFaceUseCase)

            @Suppress("UNCHECKED_CAST")
            return BulkRegistrationViewModel(bulkRegisterUseCase) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
