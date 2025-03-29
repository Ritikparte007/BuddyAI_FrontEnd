package com.example.neuroed.repository

import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.network.RetrofitClient

class NotificationRepository {

    // Suspend function to fetch notifications from the API.
    suspend fun getNotifications(): List<NotificationResponse> {
        return RetrofitClient.apiService.getNotifications()
    }
}



