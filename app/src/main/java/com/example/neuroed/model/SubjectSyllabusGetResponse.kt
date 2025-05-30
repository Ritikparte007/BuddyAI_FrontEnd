package com.example.neuroed.model

data class SubjectSyllabusGetResponse(
    val id: Int,
    val Subject: Int,
    val syllabusUnit: Int,
    val syllabusChapterName: String,
    val subjectSyllabusContent: String,
    val sallybus_completed_percentage: Int? = null
)
