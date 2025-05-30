package com.example.neuroed.model


data class SubjectSyllabusDeleteResponse(
    val success: Boolean,
    val message: String,
    val syllabusId: Int? = null,
    val status: Int = 200
)