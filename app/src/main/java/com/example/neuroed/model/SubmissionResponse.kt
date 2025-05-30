package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

data class SubmissionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)