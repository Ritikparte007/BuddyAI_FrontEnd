package com.example.neuroed.repository

import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.model.PhoneNumberVerificationResponse
import com.example.neuroed.network.ApiService

class PhoneNumberRepository(private  val apiService: ApiService){
    suspend fun PhoneNumberVerificationfun(phoneNumberVerification: PhoneNumberVerification): PhoneNumberVerificationResponse{
        return apiService.PhoneNumberVerification(phoneNumberVerification)
    }
}