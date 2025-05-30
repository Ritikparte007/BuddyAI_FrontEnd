package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

data class Meditation(
    @SerializedName("created_at") val date: String,
    @SerializedName("user")        val userId: Int,
    @SerializedName("id")          val id: Int,
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("status")     val status: String
)