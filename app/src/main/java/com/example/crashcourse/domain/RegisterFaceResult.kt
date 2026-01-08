package com.example.crashcourse.domain

sealed class RegisterFaceResult {
    object Success : RegisterFaceResult()
    data class DuplicateStudentId(val name: String) : RegisterFaceResult()
    data class DuplicateFace(val name: String) : RegisterFaceResult()
    data class Error(val message: String) : RegisterFaceResult()
}
