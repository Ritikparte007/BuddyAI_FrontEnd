package com.example.neuroed.model

data class SubjectCreateModel(
    val userId: Int,
    val image: String,
    val subjectDescription: String,
    val education: String,
    val subject: String,
    val goals: String,
    val learningTypes: String,
    val date: String,
    val time: String,
    val day: String
)



data class CreatedSubjectResponse(
    val message: String
    // Include any other fields your API returns
)