// Modified Data Models (removing @Keep annotations and User-related classes)
package com.example.neuroed.model

import com.google.gson.annotations.SerializedName

data class Challenge(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("topics") val topics: List<String> = emptyList(),
    @SerializedName("max_participants") val maxParticipants: Int = 10,
    @SerializedName("current_participants") val participantCount: Int = 1,
    @SerializedName("difficulty") val difficulty: String = "Intermediate",
    @SerializedName("is_private") val isPrivate: Boolean = false,
    @SerializedName("created_by") val createdBy: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
)

data class JoinRequestModel(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("user") val userId: Int = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("challenge_id") val challengeId: String = "",
    @SerializedName("challenge_title") val challengeTitle: String = "",
    @SerializedName("difficulty") val difficulty: String = "",
    @SerializedName("status") val status: String = "pending", // "pending", "accepted", "declined"
    @SerializedName("created_at") val createdAt: String = ""
)

// Renamed from ApiResponse to NetworkResponse
data class NetworkResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

// Local models for UI use
data class HostedChallenge(
    val id: String,
    val title: String,
    val description: String,
    val topics: List<String>,
    val maxParticipants: Int,
    val participantCount: Int,
    val difficulty: String,
    val isPrivate: Boolean
)