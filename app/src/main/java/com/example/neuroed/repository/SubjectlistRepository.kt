package com.example.neuroed.repository

import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class SubjectlistRepository(private val apiService: ApiService) {

    suspend fun getSubjectList(userId: Int): List<SubjectlistResponse> {
        return ApiHelper.executeWithToken { token ->
            apiService.getSubjectList(userId, token)
        }
    }
}