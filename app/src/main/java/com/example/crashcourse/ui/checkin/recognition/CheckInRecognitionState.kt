package com.example.crashcourse.ui.checkin.recognition

data class CheckInRecognitionState(
    val loading: Boolean = true,
    val matchName: String? = null,
    val alreadyCheckedIn: Boolean = false,
    val notRegistered: Boolean = false,
    val remainingCooldownSeconds: Long = 0L
)
