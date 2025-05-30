package com.example.neuroed.repository

import com.example.neuroed.model.Challenge
import com.example.neuroed.model.JoinRequestModel
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Challenge request model for API
data class ChallengeRequest(
    val title: String,
    val description: String,
    val topics: List<String>,
    val max_participants: Int,
    val difficulty: String,
    val is_private: Boolean,
    val current_participants: Int,
    val user_id: Int
)

// Join request model for API
data class JoinRequestRequest(
    val challenge_id: String,
    val user_id: Int
)

// Status update request
data class StatusUpdateRequest(
    val status: String,
    val user_id: Int
)

class ChallengeRepository(private val api: ApiService) {

    suspend fun createChallenge(challenge: Challenge, userId: Int): Result<Challenge> {
        return withContext(Dispatchers.IO) {
            try {
                // Create a specific request object
                val challengeRequest = ChallengeRequest(
                    title = challenge.title,
                    description = challenge.description,
                    topics = challenge.topics,
                    max_participants = challenge.maxParticipants,
                    difficulty = challenge.difficulty,
                    is_private = challenge.isPrivate,
                    current_participants = challenge.participantCount,
                    user_id = userId
                )

                // Add authentication token using ApiHelper
                val response = ApiHelper.executeWithToken { token ->
                    api.createChallenge(challengeRequest, token)
                }

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Error creating challenge: ${response.code()}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }

    suspend fun getCreatedChallenges(userId: Int): Result<List<Challenge>> {
        return withContext(Dispatchers.IO) {
            try {
                // Pass userId as a query parameter with auth token
                val response = ApiHelper.executeWithToken { token ->
                    api.getCreatedChallenges(userId, token)
                }

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Error fetching challenges: ${response.code()}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }

    suspend fun getPendingJoinRequests(userId: Int): Result<List<JoinRequestModel>> {
        return withContext(Dispatchers.IO) {
            try {
                // Pass userId as a query parameter with auth token
                val response = ApiHelper.executeWithToken { token ->
                    api.getPendingJoinRequests(userId, token)
                }

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Error fetching join requests: ${response.code()}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }

    suspend fun updateJoinRequestStatus(requestId: Int, status: String, userId: Int): Result<JoinRequestModel> {
        return withContext(Dispatchers.IO) {
            try {
                // Create a specific request object
                val statusRequest = StatusUpdateRequest(
                    status = status,
                    user_id = userId
                )

                val response = ApiHelper.executeWithToken { token ->
                    api.updateJoinRequestStatus(requestId, statusRequest, token)
                }

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Error updating join request: ${response.code()}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }

    // Additional method to join a challenge
    suspend fun joinChallenge(challengeId: String, userId: Int): Result<JoinRequestModel> {
        return withContext(Dispatchers.IO) {
            try {
                val joinRequest = JoinRequestRequest(
                    challenge_id = challengeId,
                    user_id = userId
                )

                val response = ApiHelper.executeWithToken { token ->
                    api.createJoinRequest(joinRequest, token)
                }

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Error joining challenge: ${response.code()}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }

    // Method to get joined challenges
    suspend fun getJoinedChallenges(userId: Int): Result<List<Challenge>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiHelper.executeWithToken { token ->
                    api.getJoinedChallenges(userId, token)
                }

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.status == "success" && apiResponse.data != null) {
                        Result.success(apiResponse.data)
                    } else {
                        Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                    }
                } else {
                    Result.failure(Exception("Error fetching joined challenges: ${response.code()}"))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.failure(Exception("Unexpected error: ${e.message}", e))
            }
        }
    }
}