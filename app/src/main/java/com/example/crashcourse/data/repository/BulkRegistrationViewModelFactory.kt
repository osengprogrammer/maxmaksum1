package com.example.crashcourse.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.crashcourse.data.cache.FaceCacheManager
import com.example.crashcourse.data.repository.FaceRepository
import com.example.crashcourse.db.AppDatabase
import com.example.crashcourse.domain.*
import com.example.crashcourse.domain.config.DefaultFaceMatchPolicy
import com.example.crashcourse.domain.face.FaceMatcher

class BulkRegistrationViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BulkRegistrationViewModel::class.java)) {

            // --------------------------------------------------
            // Database
            // --------------------------------------------------
            val database = AppDatabase.getInstance(application)

            // --------------------------------------------------
            // Repositories
            // --------------------------------------------------
            val faceRepository = FaceRepository(database.faceDao())
            val cacheManager = FaceCacheManager(application)

            // --------------------------------------------------
            // Biometric Core (SINGLE SOURCE OF TRUTH)
            // --------------------------------------------------
            val faceMatcher = FaceMatcher()
            val faceMatchPolicy = DefaultFaceMatchPolicy()

            // --------------------------------------------------
            // UseCases
            // --------------------------------------------------
            val registerFaceUseCase = RegisterFaceUseCase(
                cacheManager = cacheManager,
                faceRepository = faceRepository,
                duplicateDetector = FaceDuplicateDetector(
                    matcher = faceMatcher,
                    policy = faceMatchPolicy
                )
            )

            val bulkRegisterUseCase = BulkRegisterUseCase(
                registerFaceUseCase = registerFaceUseCase
            )

            @Suppress("UNCHECKED_CAST")
            return BulkRegistrationViewModel(
                bulkRegisterUseCase = bulkRegisterUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
