package com.example.crashcourse.ui.checkin

data class CheckInRecognitionState(
    val loading: Boolean = true,
    val matchName: String? = null,
    val isRegistered: Boolean = true,
    val alreadyCheckedIn: Boolean = false,
    val notRegistered: Boolean = false,
    val remainingCooldownSeconds: Long = 0L
)
