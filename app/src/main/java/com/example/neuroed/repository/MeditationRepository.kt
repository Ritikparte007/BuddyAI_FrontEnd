// ========== MeditationRepository.kt ==========
package com.example.neuroed.repository

import com.example.neuroed.model.Meditation
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

class MeditationRepository(
    private val api: ApiService
) {
    suspend fun fetchMeditations(userId: Int, onlyToday: Boolean = false): List<Meditation> {
        return ApiHelper.executeWithToken { token ->
            api.getMeditations(userId, if (onlyToday) true else null, token)
        }
    }
}