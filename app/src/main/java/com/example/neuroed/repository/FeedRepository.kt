package com.example.neuroed.repository

// FeedRepository.kt
import AgentType
import ContentType
import Feed
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRepository(private val apiService: ApiService) {

    suspend fun getFeeds(): Result<List<Feed>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.getFeeds(token)
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch feeds: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeed(id: Int): Result<Feed> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.getFeed(id, token)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Feed not found"))
            } else {
                Result.failure(Exception("Failed to fetch feed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFeed(feed: Feed): Result<Feed> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.createFeed(feed, token)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to create feed"))
            } else {
                Result.failure(Exception("Failed to create feed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFeed(id: Int, feed: Feed): Result<Feed> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.updateFeed(id, feed, token)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to update feed"))
            } else {
                Result.failure(Exception("Failed to update feed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFeed(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.deleteFeed(id, token)
            }

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete feed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeedsByAgentType(type: AgentType): Result<List<Feed>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.getFeedsByAgentType(type.value, token)
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch feeds by agent type: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFeedsByContentType(type: ContentType): Result<List<Feed>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.getFeedsByContentType(type.value, token)
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch feeds by content type: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTimeSpent(id: Int, seconds: Int): Result<Feed> = withContext(Dispatchers.IO) {
        try {
            val response = ApiHelper.executeWithToken { token ->
                apiService.updateTimeSpent(id, mapOf("spending_time_seconds" to seconds), token)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to update time spent"))
            } else {
                Result.failure(Exception("Failed to update time spent: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}