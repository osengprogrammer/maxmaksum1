package com.example.crashcourse.domain

sealed class UpdateFaceResult {
    object Success : UpdateFaceResult()
    data class Error(val message: String) : UpdateFaceResult()
}
