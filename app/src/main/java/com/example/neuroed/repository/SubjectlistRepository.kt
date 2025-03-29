package com.example.neuroed.repository

import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.network.ApiService

class SubjectlistRepository(private val apiService: ApiService) {
    suspend fun getSubjectList(userId: Int): List<SubjectlistResponse> {
        return apiService.getSubjectList(userId)
    }
}
