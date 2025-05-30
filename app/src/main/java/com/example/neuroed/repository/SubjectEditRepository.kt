package com.example.neuroed.repository

import com.example.neuroed.model.SubjectEditModel
import com.example.neuroed.model.SubjectEditResponse
import com.example.neuroed.model.SubjectModel
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import retrofit2.Response

class SubjectEditRepository(private val apiService: ApiService) {

    suspend fun getSubjectById(subjectId: Int): Response<SubjectModel> {
        return ApiHelper.executeWithToken { token ->
            apiService.getSubjectById(subjectId, token)
        }
    }

    suspend fun updateSubject(subjectEditModel: SubjectEditModel): Response<SubjectEditResponse> {
        return ApiHelper.executeWithToken { token ->
            apiService.updateSubject(subjectEditModel, token)
        }
    }
}