package com.example.crashcourse.domain

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.crashcourse.ml.StaticFaceEmbeddingPipeline
import com.example.crashcourse.utils.BulkPhotoProcessor
import com.example.crashcourse.utils.CsvImportUtils
import com.example.crashcourse.utils.PhotoStorageUtils
import com.example.crashcourse.utils.ProcessResult

/**
 * BulkRegisterUseCase
 *
 * Responsibilities:
 * - Parse CSV
 * - Loop students
 * - Resolve photo source
 * - Decode bitmap
 * - Extract face embedding (STATIC PIPELINE)
 * - Save face photo
 * - Delegate identity rules to RegisterFaceUseCase
 *
 * Used by:
 * - Bulk registration (CSV)
 * - Single photo upload (1 student = bulk size 1)
 */
class BulkRegisterUseCase(
    private val registerFaceUseCase: RegisterFaceUseCase
) {

    suspend fun estimateTime(
        context: Context,
        csvUri: Uri
    ): String? {
        return try {
            val csvResult = CsvImportUtils.parseCsvFile(context, csvUri)
            val sources = csvResult.students.map { it.photoUrl }

            val seconds = BulkPhotoProcessor.estimateProcessingTime(sources)
            when {
                seconds > 120 -> "${seconds / 60} minutes"
                seconds > 60 -> "1 minute ${seconds % 60} seconds"
                else -> "$seconds seconds"
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun execute(
        context: Context,
        csvUri: Uri,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): List<ProcessResult> {

        val csvResult = CsvImportUtils.parseCsvFile(context, csvUri)
        val students = csvResult.students
        val results = mutableListOf<ProcessResult>()

        if (students.isEmpty()) return results

        students.forEachIndexed { index, student ->
            onProgress?.invoke(index + 1, students.size)
            results.add(processSingleStudent(context, student))
        }

        return results
    }

    /**
     * Process ONE student safely.
     * This method is reusable for single upload.
     */
    suspend fun processSingleStudent(
        context: Context,
        student: CsvImportUtils.CsvStudentData
    ): ProcessResult {

        // --------------------------------------------------
        // 1. Resolve photo source
        // --------------------------------------------------
        val photoResult = BulkPhotoProcessor.processPhotoSource(
            context = context,
            photoSource = student.photoUrl,
            studentId = student.studentId
        )

        if (!photoResult.success) {
            return ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Error",
                error = photoResult.error ?: "Photo processing failed",
                photoSize = photoResult.originalSize
            )
        }

        // --------------------------------------------------
        // 2. Decode bitmap
        // --------------------------------------------------
        val bitmap = BitmapFactory.decodeFile(photoResult.localPhotoUrl)
            ?: return ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Error",
                error = "Failed to decode image",
                photoSize = photoResult.originalSize
            )

        // --------------------------------------------------
        // 3. Static face embedding pipeline
        // --------------------------------------------------
        val embeddingResult =
            StaticFaceEmbeddingPipeline.extract(bitmap)
                ?: return ProcessResult(
                    studentId = student.studentId,
                    name = student.name,
                    status = "Error",
                    error = "No valid face detected",
                    photoSize = photoResult.originalSize
                )

        val (faceBitmap, embedding) = embeddingResult

        // --------------------------------------------------
        // 4. Save face photo
        // --------------------------------------------------
        val photoPath = PhotoStorageUtils.saveFacePhoto(
            context = context,
            bitmap = faceBitmap,
            studentId = student.studentId
        ) ?: return ProcessResult(
            studentId = student.studentId,
            name = student.name,
            status = "Error",
            error = "Failed to save face photo",
            photoSize = photoResult.originalSize
        )

        // --------------------------------------------------
        // 5. Delegate registration rules
        // --------------------------------------------------
        return when (
            val result = registerFaceUseCase.execute(
                studentId = student.studentId,
                name = student.name,
                embedding = embedding,
                photoUrl = photoPath,
                className = student.className ?: "",
                subClass = student.subClass ?: "",
                grade = student.grade ?: "",
                subGrade = student.subGrade ?: "",
                program = student.program ?: "",
                role = student.role ?: ""
            )
        ) {
            is RegisterFaceResult.Success -> ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Registered",
                photoSize = photoResult.processedSize
            )

            is RegisterFaceResult.DuplicateStudentId -> ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Duplicate (ID already exists)",
                photoSize = photoResult.processedSize
            )

            is RegisterFaceResult.DuplicateFace -> ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Duplicate (Matched ${result.name})",
                photoSize = photoResult.processedSize
            )

            is RegisterFaceResult.Error -> ProcessResult(
                studentId = student.studentId,
                name = student.name,
                status = "Error",
                error = result.message,
                photoSize = photoResult.processedSize
            )
        }
    }
}
