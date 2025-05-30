package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import com.example.neuroed.model.TestUpdateRequest
import com.example.neuroed.model.TestUpdateResponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

/**
 * ViewModel for handling test-related operations
 */
class TestViewModel(
    private val apiService: ApiService
) : ViewModel() {

    /**
     * Updates the test's progress and completion status in the backend
     *
     * @param testId ID of the test to update
     * @param userId ID of the user completing the test
     * @param solveQuestion Number of questions solved/attempted
     * @param completed Whether the test is fully completed
     *
     * @return TestUpdateResponse from the server
     */
    suspend fun updateTestStatus(
        testId: Int,
        userId: Int,
        solveQuestion: Int,
        completed: Boolean
    ): TestUpdateResponse {
        val request = TestUpdateRequest(
            testId = testId,
            userId = userId,
            solveQuestion = solveQuestion,
            completed = completed
        )

        return ApiHelper.executeWithToken { token ->
        apiService.updateTestStatus(testId, request,token)

        }
    }
}