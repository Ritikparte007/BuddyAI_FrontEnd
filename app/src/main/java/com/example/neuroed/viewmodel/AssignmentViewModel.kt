package com.example.neuroed.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.neuroed.model.Assignmentdata
import com.example.neuroed.repository.AssignmentRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AssignmentListViewModel(
    private val repository: AssignmentRepository,
    val userId: Int
) : ViewModel() {

    private val _assignments = MutableLiveData<List<Assignmentdata>>(emptyList())
    val assignments: LiveData<List<Assignmentdata>> = _assignments

    init {
        loadAssignments()
    }

    fun loadAssignments(onlyPending: Boolean = false) {
        viewModelScope.launch {
            Log.d("AssignmentListVM", "Fetching assignments for userId=$userId, onlyPending=$onlyPending")
            try {
                val list = repository.fetchAssignments(userId)
                    .let { if (onlyPending) it.filter { a -> a.status != "completed" } else it }

                Log.d("AssignmentListVM", "Fetched: $list")
                Log.d("AssignmentListVM", "JSON:\n${Gson().toJson(list)}")

                _assignments.value = list
            } catch (e: Exception) {
                Log.e("AssignmentListVM", "Failed to load assignments", e)
                _assignments.value = emptyList()
            }
        }
    }

    fun submitAssignment(
        assignmentId: Int,
        fileUri: Uri?,
        answerText: String,
        context: Context,
        onStartUpload: () -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Signal upload started
                onStartUpload()

                val result = repository.submitAssignment(
                    assignmentId = assignmentId,
                    userId = userId,
                    fileUri = fileUri,
                    answerText = answerText,
                    context = context
                )

                if (result.isSuccess) {
                    // Update local assignment status
                    updateAssignmentStatus(assignmentId, "completed")
                    onSuccess()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    onError(error)
                }
            } catch (e: Exception) {
                Log.e("AssignmentListVM", "Submission failed", e)
                onError(e.message ?: "Submission failed")
            }
        }
    }

    // Update local status without server call
    private fun updateAssignmentStatus(assignmentId: Int, newStatus: String) {
        val currentList = _assignments.value ?: emptyList()
        val updatedList = currentList.map { assignment ->
            if (assignment.id == assignmentId) {
                assignment.copy(status = newStatus)
            } else {
                assignment
            }
        }
        _assignments.value = updatedList
    }
}