package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.SubjectEditModel
import com.example.neuroed.model.SubjectEditResponse
import com.example.neuroed.model.SubjectModel
import com.example.neuroed.repository.SubjectEditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubjectEditViewModel(private val repository: SubjectEditRepository) : ViewModel() {

    private val _subject = MutableStateFlow<SubjectModel?>(null)
    val subject: StateFlow<SubjectModel?> = _subject

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var updatedSubjectResponse: SubjectEditResponse? = null
        private set

    fun getSubjectById(subjectId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getSubjectById(subjectId)
                if (response.isSuccessful) {
                    _subject.value = response.body()
                } else {
                    _error.value = "Failed to get subject details: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSubject(subjectEditModel: SubjectEditModel) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.updateSubject(subjectEditModel)
                if (response.isSuccessful) {
                    updatedSubjectResponse = response.body()
                } else {
                    _error.value = "Failed to update subject: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class SubjectEditViewModelFactory(private val repository: SubjectEditRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectEditViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}