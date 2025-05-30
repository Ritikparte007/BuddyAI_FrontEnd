package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

// Request to update test progress
data class TestUpdateRequest(
    @SerializedName("test_id")
    val testId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("solve_question")
    val solveQuestion: Int,

    @SerializedName("completed")
    val completed: Boolean
)

// Response from the server
data class TestUpdateResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("test_id")
    val testId: Int? = null,

    @SerializedName("solve_question")
    val solve_question: Int? = null,

    @SerializedName("completed")
    val completed: Boolean? = null
)