package com.example.crashcourse.ui.checkin.recognition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.db.AppDatabase
import com.example.crashcourse.db.CheckInRecord
import com.example.crashcourse.db.CheckInRecordDao
import com.example.crashcourse.db.FaceCache
import com.example.crashcourse.domain.FaceRecognitionUseCase
import com.example.crashcourse.domain.config.DefaultFaceMatchPolicy
import com.example.crashcourse.domain.face.FaceMatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class CheckInRecognitionViewModel(
    application: Application
) : AndroidViewModel(application) {

    // --------------------------------------------------
    // Biometric Core (SINGLE SOURCE OF TRUTH)
    // --------------------------------------------------
    private val faceMatcher = FaceMatcher()
    private val faceMatchPolicy = DefaultFaceMatchPolicy()

    // --------------------------------------------------
    // DOMAIN
    // --------------------------------------------------
    private val recognitionUseCase = FaceRecognitionUseCase(
        matcher = faceMatcher,
        policy = faceMatchPolicy
    )

    private val checkInPolicy = CheckInPolicy(cooldownMinutes = 2)

    // --------------------------------------------------
    // DATABASE
    // --------------------------------------------------
    private val checkInRecordDao: CheckInRecordDao =
        AppDatabase.getInstance(application).checkInRecordDao()

    // --------------------------------------------------
    // CACHE
    // --------------------------------------------------
    // Pair<studentId, embedding>
    private var gallery: List<Pair<String, FloatArray>> = emptyList()

    // --------------------------------------------------
    // UI STATE
    // --------------------------------------------------
    private val _state = MutableStateFlow(CheckInRecognitionState())
    val state: StateFlow<CheckInRecognitionState> = _state

    init {
        loadGallery()
        startCooldownTicker()
    }

    private fun loadGallery() {
        viewModelScope.launch {
            gallery = FaceCache.load(getApplication())
            _state.value = _state.value.copy(loading = false)
        }
    }

    /**
     * ENTRY POINT
     * Called when camera/analyzer emits an embedding
     */
    fun onEmbeddingReceived(embedding: FloatArray) {
        val matchedStudentId =
            recognitionUseCase.findBestMatch(gallery, embedding)

        if (matchedStudentId != null) {
            handleRecognizedFace(matchedStudentId)
        } else {
            _state.value = CheckInRecognitionState(
    loading = false,
    matchName = null,
    alreadyCheckedIn = false,
    notRegistered = true,
    remainingCooldownSeconds = 0L
)

        }
    }

    /**
     * Handles recognized face + cooldown decision
     */
    private fun handleRecognizedFace(studentId: String) {
        viewModelScope.launch {
            val now = LocalDateTime.now()

            // 🔑 DB IS SOURCE OF TRUTH
            val lastCheckInTime =
                checkInRecordDao.getLastCheckInTime(studentId)

            val canCheckIn =
                checkInPolicy.canCheckIn(lastCheckInTime, now)

            val remainingSeconds =
                if (!canCheckIn)
                    checkInPolicy.remainingSeconds(lastCheckInTime, now)
                else 0L

            if (canCheckIn) {
                persistCheckIn(studentId, now)
            }

            _state.value = CheckInRecognitionState(
                loading = false,
                matchName = studentId,
                alreadyCheckedIn = !canCheckIn,
                notRegistered = false,
                remainingCooldownSeconds = remainingSeconds
            )
        }
    }

    /**
     * REAL ATTENDANCE WRITE
     */
    private suspend fun persistCheckIn(
        studentId: String,
        timestamp: LocalDateTime
    ) {
        val record = CheckInRecord(
            studentId = studentId,
            name = studentId, // later map from FaceEntity
            timestamp = timestamp,
            classId = null,
            subClassId = null,
            gradeId = null,
            subGradeId = null,
            programId = null,
            roleId = null
        )

        checkInRecordDao.insert(record)
    }

    /**
     * GLOBAL COOLDOWN TICKER
     * Decrements remaining seconds safely every second
     */
    private fun startCooldownTicker() {
        viewModelScope.launch {
            while (true) {
                val current = _state.value
                if (
                    current.alreadyCheckedIn &&
                    current.remainingCooldownSeconds > 0
                ) {
                    _state.value = current.copy(
                        remainingCooldownSeconds =
                            current.remainingCooldownSeconds - 1
                    )
                }
                delay(1000)
            }
        }
    }
}
