package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.SubjectDeleteResponse
import com.example.neuroed.model.SubjectSyllabusDeleteResponse
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import kotlinx.coroutines.launch

class SubjectSyllabusViewModel(
    private val repository: SubjectSyllabusGetRepository,
    private val subjectId: Int
) : ViewModel() {

    private val _subjectSyllabus = MutableLiveData<List<SubjectSyllabusGetResponse>>()
    val subjectSyllabus: LiveData<List<SubjectSyllabusGetResponse>>
        get() = _subjectSyllabus

    // Loading state to show a loading indicator if needed
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Delete operation states
    private val _deleteUnitResult = MutableLiveData<SubjectSyllabusDeleteResponse?>()
    val deleteUnitResult: LiveData<SubjectSyllabusDeleteResponse?> = _deleteUnitResult

    private val _deleteSubjectResult = MutableLiveData<SubjectDeleteResponse?>()
    val deleteSubjectResult: LiveData<SubjectDeleteResponse?> = _deleteSubjectResult

    init {
        fetchSubjectSyllabus()
    }

    private fun fetchSubjectSyllabus() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val syllabusList = repository.getSubjectSyllabusGet(subjectId)
                Log.d("SubjectSyllabusViewModel", "Fetched syllabus data: $syllabusList")
                _subjectSyllabus.value = syllabusList
            } catch (e: Exception) {
                Log.e("SubjectSyllabusViewModel", "Error fetching syllabus data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add this public method to refresh data
    fun refreshSyllabus() {
        fetchSubjectSyllabus()
    }

    // Add this alias method to match what's called in the UI
    fun loadSyllabus() {
        fetchSubjectSyllabus()
    }

    // Method to delete a syllabus unit
    fun deleteUnit(syllabusId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.deleteSubjectSyllabusUnit(syllabusId)
                _deleteUnitResult.value = result

                // Refresh the syllabus list if deletion was successful
                if (result.success) {
                    refreshSyllabus()
                }

                Log.d("SubjectSyllabusViewModel", "Unit deletion result: $result")
            } catch (e: Exception) {
                Log.e("SubjectSyllabusViewModel", "Error deleting syllabus unit", e)
                _deleteUnitResult.value = SubjectSyllabusDeleteResponse(
                    success = false,
                    message = "Error: ${e.message}",
                    status = 500
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Method to delete the entire subject
    fun deleteSubject() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.deleteSubject(subjectId)
                _deleteSubjectResult.value = result

                Log.d("SubjectSyllabusViewModel", "Subject deletion result: $result")
            } catch (e: Exception) {
                Log.e("SubjectSyllabusViewModel", "Error deleting subject", e)
                _deleteSubjectResult.value = SubjectDeleteResponse(
                    success = false,
                    message = "Error: ${e.message}",
                    status = 500
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Reset the delete results (to be called after handling the result)
    fun resetDeleteResults() {
        _deleteUnitResult.value = null
        _deleteSubjectResult.value = null
    }
}