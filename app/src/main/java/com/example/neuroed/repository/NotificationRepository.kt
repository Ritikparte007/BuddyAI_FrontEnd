package com.example.neuroed.repository

import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.RetrofitClient

class NotificationRepository {

    // Suspend function to fetch notifications from the API with authentication.
    suspend fun getNotifications(): List<NotificationResponse> {
        return ApiHelper.executeWithToken { token ->
            RetrofitClient.apiService.getNotifications(token)
        }
    }
}