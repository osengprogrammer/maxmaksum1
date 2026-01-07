package com.example.crashcourse.domain

import java.time.Duration
import java.time.LocalDateTime

class CheckInPolicy(
    private val cooldownMinutes: Long = 2
) {

    fun canCheckIn(
        lastCheckInTime: LocalDateTime?,
        now: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        if (lastCheckInTime == null) return true
        return Duration.between(lastCheckInTime, now).toMinutes() >= cooldownMinutes
    }

    fun remainingSeconds(
        lastCheckInTime: LocalDateTime?,
        now: LocalDateTime = LocalDateTime.now()
    ): Long {
        if (lastCheckInTime == null) return 0

        val elapsedSeconds =
            Duration.between(lastCheckInTime, now).seconds

        val cooldownSeconds = cooldownMinutes * 60
        return (cooldownSeconds - elapsedSeconds).coerceAtLeast(0)
    }
}
