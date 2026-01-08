package com.example.crashcourse.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.data.cache.FaceCacheManager
import com.example.crashcourse.data.repository.FacePhotoRepository
import com.example.crashcourse.data.repository.FaceRepository
import com.example.crashcourse.db.*
import com.example.crashcourse.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FaceViewModel(application: Application) : AndroidViewModel(application) {

    // --------------------------------------------------
    // Database
    // --------------------------------------------------
    private val database = AppDatabase.getInstance(application)

    // --------------------------------------------------
    // Repositories
    // --------------------------------------------------
    private val faceRepository = FaceRepository(database.faceDao())
    private val photoRepository = FacePhotoRepository(application)
    private val cacheManager = FaceCacheManager(application)

    // --------------------------------------------------
    // UseCases
    // --------------------------------------------------
    private val registerFaceUseCase = RegisterFaceUseCase(
    cacheManager = cacheManager,
    faceRepository = faceRepository,
    duplicateDetector = FaceDuplicateDetector(threshold = 0.3f)
)

    private val updateFaceUseCase = UpdateFaceUseCase(
        faceRepository = faceRepository,
        photoRepository = photoRepository,
        cacheManager = cacheManager
    )

    private val deleteFaceUseCase = DeleteFaceUseCase(
        repository = faceRepository,
        cacheManager = cacheManager
    )

    // --------------------------------------------------
    // UI State (Flows)
    // --------------------------------------------------

    val faceList: StateFlow<List<FaceEntity>> =
        database.faceDao()
            .getAllFacesFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val classOptions: StateFlow<List<ClassOption>> =
        database.classOptionDao()
            .getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val gradeOptions: StateFlow<List<GradeOption>> =
        database.gradeOptionDao()
            .getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val programOptions: StateFlow<List<ProgramOption>> =
        database.programOptionDao()
            .getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val roleOptions: StateFlow<List<RoleOption>> =
        database.roleOptionDao()
            .getAllOptions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --------------------------------------------------
    // Dependent Dropdowns
    // --------------------------------------------------

    fun getSubClassOptions(parentClassId: Int): Flow<List<SubClassOption>> {
        return database.subClassOptionDao().getOptionsForClass(parentClassId)
    }

    fun getSubGradeOptions(parentGradeId: Int): Flow<List<SubGradeOption>> {
        return database.subGradeOptionDao().getOptionsForGrade(parentGradeId)
    }

    // --------------------------------------------------
    // UI Actions
    // --------------------------------------------------

    fun registerFace(
        studentId: String,
        name: String,
        embedding: FloatArray,
        photoUrl: String?,
        className: String,
        subClass: String,
        grade: String,
        subGrade: String,
        program: String,
        role: String,
        onSuccess: () -> Unit,
        onDuplicate: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (
                val result = registerFaceUseCase.execute(
                    studentId = studentId,
                    name = name,
                    embedding = embedding,
                    photoUrl = photoUrl,
                    className = className,
                    subClass = subClass,
                    grade = grade,
                    subGrade = subGrade,
                    program = program,
                    role = role
                )
            ) {
                is RegisterFaceResult.Success -> onSuccess()
                is RegisterFaceResult.DuplicateStudentId -> onDuplicate(result.name)
                is RegisterFaceResult.DuplicateFace -> onDuplicate(result.name)
                is RegisterFaceResult.Error -> onError(result.message)
            }
        }
    }

    fun updateFace(
        face: FaceEntity,
        photo: Bitmap?,
        embedding: FloatArray,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = updateFaceUseCase.execute(face, photo, embedding)) {
                is UpdateFaceResult.Success -> onComplete()
                is UpdateFaceResult.Error -> onError(result.message)
            }
        }
    }

    fun deleteFace(face: FaceEntity) {
        viewModelScope.launch {
            deleteFaceUseCase.execute(face)
        }
    }

    // --------------------------------------------------
    // Backward Compatibility (Optional)
    // --------------------------------------------------

    fun registerFace(
        name: String,
        embedding: FloatArray,
        onSuccess: () -> Unit,
        onDuplicate: (String) -> Unit
    ) {
        val studentId = "STU${System.currentTimeMillis()}"
        registerFace(
            studentId = studentId,
            name = name,
            embedding = embedding,
            photoUrl = null,
            className = "",
            subClass = "",
            grade = "",
            subGrade = "",
            program = "",
            role = "",
            onSuccess = onSuccess,
            onDuplicate = onDuplicate
        )
    }
}
