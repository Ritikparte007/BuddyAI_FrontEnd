package com.example.neuroed.repository

import com.example.neuroed.model.UserAppVisitData
import com.example.neuroed.model.Uservisitdatarespponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

class UservisitRepository(private val apiService: ApiService){
    suspend fun Uservisitdatasave(userAppVisitData: UserAppVisitData): Uservisitdatarespponse{
        return apiService.uservisitdatafun(userAppVisitData)
    }
}