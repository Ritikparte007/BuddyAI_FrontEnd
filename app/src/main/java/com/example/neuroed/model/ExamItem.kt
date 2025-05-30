package com.example.neuroed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExamItem(
    val id: Int,
    val user_id: Int,
    val subject_name: String,
    val unit: Int?,
    val unit_name: String?,
    val logo: String?, // URL path to the image
    val bad: Int?,
    val Average: Int?,
    val Good: Int?,
    val Excellence: Int?,
    val Exam_duration_time: String?,
    val Exam_marks: Int?,
    val TimeCountDown: String?,
    val Difficulty: String?,
    val TotalQuestion: Int?,
    var SolveQuestion: Int?, // Changed to 'var' so we can update it
    var Completed: Boolean = false,
    val end_time: Long, // Store as milliseconds since epoch
    val exam_type: String? // One of "MCQ", "FILL_BLANK", "SHORT_ANSWER", "TRUE_FALSE"
) : Parcelable {
    val totalQuestion: Int
        get() = TotalQuestion ?: 0

    // Helper method to check if exam is expired
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > end_time
    }

    // Helper method to get time remaining in formatted string
    fun getTimeRemaining(): String {
        val timeRemaining = end_time - System.currentTimeMillis()
        if (timeRemaining <= 0) return "Expired"

        val hours = timeRemaining / (1000 * 60 * 60)
        val minutes = (timeRemaining % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            hours > 0 -> "$hours hr ${minutes} min"
            else -> "$minutes min"
        }
    }

    // Get the exam type enum from string
    fun getExamTypeEnum(): TestType {
        return when(exam_type) {
            "MCQ" -> TestType.MCQ
            "FILL_BLANK" -> TestType.FILL_BLANK
            "SHORT_ANSWER" -> TestType.SHORT_ANSWER
            "TRUE_FALSE" -> TestType.TRUE_FALSE
            else -> TestType.MCQ // Default to MCQ
        }
    }
}