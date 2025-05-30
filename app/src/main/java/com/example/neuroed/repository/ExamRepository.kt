package com.example.neuroed.repository

import com.example.neuroed.model.ExamDto
import com.example.neuroed.model.ExamUiModel
import com.example.neuroed.model.toUiModel
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository class for handling exam-related data operations
 * Acts as a single source of truth for exam data
 */
class ExamRepository(private val apiService: ApiService) {

    /**
     * Fetch pending (active) exams for a user
     * @param userId The ID of the user
     * @return Flow of Result containing a list of ExamUiModel
     */
    fun getPendingExams(userId: Int): Flow<Result<List<ExamUiModel>>> = flow {
        try {
            // Make API call to fetch exams with authentication
            val exams = ApiHelper.executeWithToken { token ->
                apiService.getExams(userId, token)
            }

            // Filter for active exams (not completed) and convert to UI models
            val pendingExams = exams
                .filter { it.completed == false } // Not completed means pending
                .map { it.toUiModel() }

            // Emit successful result
            emit(Result.success(pendingExams))
        } catch (e: IOException) {
            // Network error
            emit(Result.failure(e))
        } catch (e: HttpException) {
            // HTTP error
            emit(Result.failure(e))
        } catch (e: Exception) {
            // Other errors
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Fetch completed (inactive) exams for a user
     * @param userId The ID of the user
     * @return Flow of Result containing a list of ExamUiModel
     */
    fun getCompletedExams(userId: Int): Flow<Result<List<ExamUiModel>>> = flow {
        try {
            // Make API call to fetch exams with authentication
            val exams = ApiHelper.executeWithToken { token ->
                apiService.getExams(userId, token)
            }

            // Filter for completed exams and convert to UI models
            val completedExams = exams
                .filter { it.completed == true } // Completed means inactive
                .map { it.toUiModel() }

            // Emit successful result
            emit(Result.success(completedExams))
        } catch (e: IOException) {
            // Network error
            emit(Result.failure(e))
        } catch (e: HttpException) {
            // HTTP error
            emit(Result.failure(e))
        } catch (e: Exception) {
            // Other errors
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Get a specific exam by ID
     * @param examId The ID of the exam
     * @return Flow of Result containing an ExamUiModel
     */
    fun getExamById(examId: Int): Flow<Result<ExamUiModel>> = flow {
        try {
            // Make API call to fetch the exam with authentication
            val exam = ApiHelper.executeWithToken { token ->
                apiService.getExamById(examId, token)
            }

            // Convert to UI model
            val examUiModel = exam.toUiModel()

            // Emit successful result
            emit(Result.success(examUiModel))
        } catch (e: IOException) {
            // Network error
            emit(Result.failure(e))
        } catch (e: HttpException) {
            // HTTP error
            emit(Result.failure(e))
        } catch (e: Exception) {
            // Other errors
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}