package com.example.neuroed.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data model representing an Exam from the backend
 * Matches the Django model structure
 */
data class ExamDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("user_id_id")  // Changed to match Django's foreign key field name
    val userId: Int?,

    @SerializedName("subject_name")
    val subjectName: String?,

    @SerializedName("unit")
    val unit: Int?,

    @SerializedName("unit_name")
    val unitName: String?,

    @SerializedName("logo")
    val logoUrl: String?,

    @SerializedName("bad")
    val bad: Int?,

    @SerializedName("Average")
    val average: Int?,

    @SerializedName("Good")
    val good: Int?,

    @SerializedName("Excellence")
    val excellence: Int?,

    @SerializedName("Exam_duration_time")
    val examDurationTime: String?,

    @SerializedName("Exam_marks")
    val examMarks: Int?,

    @SerializedName("Create_Exam_date")
    val createExamDate: String?,

    @SerializedName("TimeCountDown")
    val timeCountDown: String?,

    @SerializedName("Difficulty")
    val difficulty: String?,

    @SerializedName("TotalQuestion")
    val totalQuestion: Int?,

    @SerializedName("SolveQuestion")
    val solveQuestion: Int?,

    @SerializedName("Completed")
    val completed: Boolean?,

    @SerializedName("end_time")
    val endTime: String?,

    @SerializedName("exam_type")
    val examType: String?
) : Serializable

/**
 * UI model for presenting Exam data on the screen
 * Transforms raw data from API into format needed for the UI
 */
data class ExamUiModel(
    val id: Int,
    val subject: String,
    val unitWithName: String,
    val marks: Int,
    val logoUrl: String?,

    // Performance thresholds as normalized values (0.0 - 1.0)
    val badThreshold: Float,
    val averageThreshold: Float,
    val goodThreshold: Float,
    val excellenceThreshold: Float,

    // Countdown in milliseconds
    val remainingTimeMs: Long,

    val totalQuestion: Int?,
    val solveQuestion: Int?,

    // Status
    val isActive: Boolean,
    val createDate: String
) : Serializable

/**
 * Extension function to convert ExamDto to ExamUiModel
 */
fun ExamDto.toUiModel(): ExamUiModel {
    // Calculate the maximum possible score for normalizing thresholds
    val maxScore = 100 // Default to 100 if not specified

    // Calculate remaining time based on end_time if available
    val remainingTimeMs = try {
        if (endTime != null) {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault())
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val endDate = format.parse(endTime)
            val now = java.util.Date()

            val diffMs = endDate.time - now.time
            if (diffMs > 0) diffMs else 0L
        } else {
            172_800_000L // 2 days in milliseconds as fallback
        }
    } catch (e: Exception) {
        e.printStackTrace()
        172_800_000L // Fallback if parsing fails
    }

    return ExamUiModel(
        id = id,
        subject = subjectName ?: "Unknown Subject",
        unitWithName = if (unitName != null) {
            "Unit ${unit ?: ""}: $unitName"
        } else {
            "Unit ${unit ?: ""}"
        },
        marks = examMarks ?: 0,
        logoUrl = logoUrl,

        // Normalize score thresholds to 0.0-1.0 range
        badThreshold = bad?.toFloat()?.div(maxScore) ?: 0.2f,
        averageThreshold = average?.toFloat()?.div(maxScore) ?: 0.5f,
        goodThreshold = good?.toFloat()?.div(maxScore) ?: 0.7f,
        excellenceThreshold = excellence?.toFloat()?.div(maxScore) ?: 0.9f,


        totalQuestion = totalQuestion,
        solveQuestion = solveQuestion,


        remainingTimeMs = remainingTimeMs,
        isActive = completed?.not() ?: true, // If not completed, it's active
        createDate = createExamDate ?: "Unknown"
    )
}



/**
 * Extension function to convert ExamUiModel to ExamItem
 */
fun ExamUiModel.toExamItem(userId: Int): ExamItem {
    // Parse end time to milliseconds
    val endTimeMs = System.currentTimeMillis() + remainingTimeMs

    return ExamItem(
        id = id,
        user_id = userId,
        subject_name = subject,
        unit = extractUnitNumber(unitWithName),
        unit_name = extractUnitName(unitWithName),
        logo = logoUrl,
        bad = (badThreshold * 100).toInt(),
        Average = (averageThreshold * 100).toInt(),
        Good = (goodThreshold * 100).toInt(),
        Excellence = (excellenceThreshold * 100).toInt(),
        Exam_duration_time = null, // Not available in ExamUiModel
        Exam_marks = marks,
        TimeCountDown = formatTimeCountdown(remainingTimeMs),
        Difficulty = null, // Not available in ExamUiModel
        TotalQuestion = totalQuestion ?: 5,
        SolveQuestion = solveQuestion ?: 0,
        Completed = !isActive,
        end_time = endTimeMs,
        exam_type = "MCQ" // Default to MCQ since not available in ExamUiModel
    )
}

// Helper function to extract unit number from unitWithName
private fun extractUnitNumber(unitWithName: String): Int? {
    // Parse "Unit X: Name" to get X
    return try {
        val regex = "Unit (\\d+).*".toRegex()
        val matchResult = regex.find(unitWithName)
        matchResult?.groupValues?.get(1)?.toIntOrNull()
    } catch (e: Exception) {
        null
    }
}

// Helper function to extract unit name from unitWithName
private fun extractUnitName(unitWithName: String): String? {
    // Parse "Unit X: Name" to get Name
    return try {
        val regex = "Unit \\d+:\\s*(.*)".toRegex()
        val matchResult = regex.find(unitWithName)
        matchResult?.groupValues?.get(1)
    } catch (e: Exception) {
        null
    }
}

// Helper function to format time countdown
private fun formatTimeCountdown(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        else -> "${minutes % 60}m ${seconds % 60}s"
    }
}


