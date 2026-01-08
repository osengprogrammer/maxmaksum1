package com.example.crashcourse.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crashcourse.domain.BulkRegisterUseCase
import com.example.crashcourse.utils.BulkPhotoProcessor
import com.example.crashcourse.utils.CsvImportUtils
import com.example.crashcourse.utils.ProcessResult
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
     * UI-only preparation step:
     * - parse CSV
     * - estimate processing time
     */
    fun prepareProcessing(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val csvResult = CsvImportUtils.parseCsvFile(context, uri)
                val photoSources = csvResult.students.map { it.photoUrl }

                val seconds = BulkPhotoProcessor.estimateProcessingTime(photoSources)
                val estimate = when {
                    seconds > 120 -> "${seconds / 60} minutes"
                    seconds > 60 -> "1 minute ${seconds % 60} seconds"
                    else -> "$seconds seconds"
                }

                _state.value = _state.value.copy(
                    estimatedTime = "Estimated time: $estimate"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    estimatedTime = "Time estimate unavailable"
                )
            }
        }
    }

    /**
     * Main bulk registration entry point
     * Delegates ALL work to BulkRegisterUseCase
     */
    fun processCsvFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isProcessing = true,
                progress = 0f,
                status = "Starting bulk registration..."
            )

            val results: List<ProcessResult> =
                bulkRegisterUseCase.execute(
                    context = context,
                    csvUri = uri
                ) { current, total ->
                    _state.value = _state.value.copy(
                        progress = current.toFloat() / total,
                        status = "Processing $current / $total"
                    )
                }

            val successCount = results.count { it.status == "Registered" }
            val duplicateCount = results.count { it.status.startsWith("Duplicate") }
            val errorCount = results.count { it.status == "Error" }

            _state.value = _state.value.copy(
                isProcessing = false,
                results = results,
                successCount = successCount,
                duplicateCount = duplicateCount,
                errorCount = errorCount,
                status = "Processed $successCount users successfully"
            )
        }
    }

    fun resetState() {
        _state.value = ProcessingState()
    }
}
