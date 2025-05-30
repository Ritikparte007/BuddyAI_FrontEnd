package com.example.neuroed.repository

import com.example.neuroed.model.CreatedSubjectResponse
import com.example.neuroed.model.SubjectCreateModel
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

class SubjectCreateRepository(private val apiService: ApiService) {

    suspend fun createSubject(subjectCreateModel: SubjectCreateModel): CreatedSubjectResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.subjectCreate(subjectCreateModel, token)
        }
    }
}