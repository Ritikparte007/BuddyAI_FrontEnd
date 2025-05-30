package com.example.neuroed.repository

import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.model.PhoneNumberVerificationResponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

class PhoneNumberRepository(private val apiService: ApiService) {
    suspend fun PhoneNumberVerificationfun(phoneNumberVerification: PhoneNumberVerification): PhoneNumberVerificationResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.PhoneNumberVerification(phoneNumberVerification, token)
        }
    }
}