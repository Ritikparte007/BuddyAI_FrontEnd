package com.example.neuroed.repository

import com.example.neuroed.model.testnotificationmodel


import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import com.example.neuroed.network.RetrofitClient

class TestNotificationModelPredicationRepository(private val apiService: ApiService){

    // Suspend function to fetch notifications from the API.
    suspend fun testNotificationpredication(userid: Int): List<testnotificationmodel> {
        return  ApiHelper.executeWithToken { token ->
        RetrofitClient.apiService.TestModelpredication(userid, token)
        }
    }
}
