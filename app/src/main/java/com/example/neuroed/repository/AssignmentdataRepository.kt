package com.example.neuroed.repository

import android.content.Context
import android.net.Uri
import com.example.neuroed.AssignmentSubmissionService
import com.example.neuroed.model.Assignmentdata
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

class AssignmentRepository(
    private val api: ApiService
) {
    suspend fun fetchAssignments(userId: Int): List<Assignmentdata> {
        return ApiHelper.executeWithToken { token ->
            api.getAssignments(userId, token)
        }
    }

    suspend fun submitAssignment(
        assignmentId: Int,
        userId: Int,
        fileUri: Uri?,
        answerText: String,
        context: Context
    ): Result<Boolean> {
        val submissionService = AssignmentSubmissionService(context, api)
        return submissionService.submitAssignment(
            assignmentId = assignmentId,
            userId = userId,
            fileUri = fileUri,
            answerText = answerText
        )
    }
}