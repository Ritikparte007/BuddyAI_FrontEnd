package com.example.neuroed.model

data class SubjectEditModel(
    val id: Int,
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

data class SubjectEditResponse(
    val status: Boolean,
    val message: String,
    val data: SubjectModel?
)


// Assuming this model already exists, but defining it here for clarity
data class SubjectModel(
    val id: Int,
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
    // There might be additional fields in the original model
)