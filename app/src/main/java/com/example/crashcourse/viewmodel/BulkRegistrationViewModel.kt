package com.example.crashcourse.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.domain.BulkRegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class BulkRegistrationViewModel(
    private val bulkRegisterUseCase: BulkRegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProcessingState())
    val state: StateFlow<ProcessingState> = _state.asStateFlow()

    /**
     * UI-only preparation step
     * Delegates estimation to UseCase
     */
    fun prepareProcessing(context: Context, uri: Uri) {
        viewModelScope.launch {
            val estimate = bulkRegisterUseCase.estimateTime(context, uri)
            _state.value = _state.value.copy(
                estimatedTime = estimate?.let { "Estimated time: $it" }
                    ?: "Time estimate unavailable"
            )
        }
    }

    /**
     * Main bulk registration entry point
     */
    fun processCsvFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isProcessing = true,
                progress = 0f,
                status = "Starting bulk registration..."
            )

            val results =
                bulkRegisterUseCase.execute(
                    context = context,
                    csvUri = uri
                ) { current, total ->
                    _state.value = _state.value.copy(
                        progress = current.toFloat() / total,
                        status = "Processing $current / $total"
                    )
                }

            _state.value = _state.value.copy(
                isProcessing = false,
                results = results,
                successCount = results.count { it.status == "Registered" },
                duplicateCount = results.count { it.status.startsWith("Duplicate") },
                errorCount = results.count { it.status == "Error" },
                status = "Processed ${results.count { it.status == "Registered" }} users successfully"
            )
        }
    }

    fun resetState() {
        _state.value = ProcessingState()
    }
}
