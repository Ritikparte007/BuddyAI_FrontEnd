package com.example.neuroed.model

data class SubjectDeleteResponse(
    val success: Boolean,
    val message: String,
    val subjectId: Int? = null,
    val status: Int = 200
)