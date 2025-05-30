package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.ExamUiModel
import com.example.neuroed.repository.ExamRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Exam-related UI state and business logic
 */
class ExamViewModel(
    private val examRepository: ExamRepository,
    private val userId: Int
) : ViewModel() {

    // UI State for Pending Exams
    private val _pendingExamsState = MutableStateFlow<ExamsUiState>(ExamsUiState.Loading)
    val pendingExamsState: StateFlow<ExamsUiState> = _pendingExamsState.asStateFlow()

    // UI State for Completed Exams
    private val _completedExamsState = MutableStateFlow<ExamsUiState>(ExamsUiState.Loading)
    val completedExamsState: StateFlow<ExamsUiState> = _completedExamsState.asStateFlow()

    // Initialize by loading exams
    init {
        loadPendingExams()
        loadCompletedExams()
    }

    /**
     * Load pending (active) exams for the user
     */
    fun loadPendingExams() {
        viewModelScope.launch {
            Log.d("ExamViewModel", "Loading pending exams for user: $userId")
            _pendingExamsState.value = ExamsUiState.Loading

            examRepository.getPendingExams(userId)
                .catch { e ->
                    Log.e("ExamViewModel", "Error loading pending exams", e)
                    _pendingExamsState.value = ExamsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { exams ->
                            Log.d("ExamViewModel", "Loaded ${exams.size} pending exams")
                            if (exams.isEmpty()) {
                                _pendingExamsState.value = ExamsUiState.Empty
                            } else {
                                _pendingExamsState.value = ExamsUiState.Success(exams)
                            }
                        },
                        onFailure = { e ->
                            Log.e("ExamViewModel", "Failed to load pending exams", e)
                            _pendingExamsState.value = ExamsUiState.Error(e.message ?: "Unknown error")
                        }
                    )
                }
        }
    }

    /**
     * Load completed (inactive) exams for the user
     */
    fun loadCompletedExams() {
        viewModelScope.launch {
            Log.d("ExamViewModel", "Loading completed exams for user: $userId")
            _completedExamsState.value = ExamsUiState.Loading

            examRepository.getCompletedExams(userId)
                .catch { e ->
                    Log.e("ExamViewModel", "Error loading completed exams", e)
                    _completedExamsState.value = ExamsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { exams ->
                            Log.d("ExamViewModel", "Loaded ${exams.size} completed exams")
                            if (exams.isEmpty()) {
                                _completedExamsState.value = ExamsUiState.Empty
                            } else {
                                _completedExamsState.value = ExamsUiState.Success(exams)
                            }
                        },
                        onFailure = { e ->
                            Log.e("ExamViewModel", "Failed to load completed exams", e)
                            _completedExamsState.value = ExamsUiState.Error(e.message ?: "Unknown error")
                        }
                    )
                }
        }
    }

    /**
     * Refresh both pending and completed exams
     */
    fun refreshExams() {
        loadPendingExams()
        loadCompletedExams()
    }

    /**
     * Get current user ID
     */
    fun getUserId(): Int = userId
}

/**
 * Sealed class representing the UI state for exam lists
 */
sealed class ExamsUiState {
    object Loading : ExamsUiState()
    object Empty : ExamsUiState()
    data class Success(val exams: List<ExamUiModel>) : ExamsUiState()
    data class Error(val message: String) : ExamsUiState()
}