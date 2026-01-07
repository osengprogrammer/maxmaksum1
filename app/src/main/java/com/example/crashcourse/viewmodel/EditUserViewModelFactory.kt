package com.example.crashcourse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditUserViewModelFactory(
    private val faceViewModel: FaceViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditUserViewModel(faceViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
