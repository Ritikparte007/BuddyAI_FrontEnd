package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

data class SubjectlistResponse(
    val id: Int,
    val subject: String,
    val subjectDescription: String,
    @SerializedName("subject_img")
    val subjectImage: String? = null
)
