package com.example.neuroed.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.CreatedSubjectResponse
import com.example.neuroed.model.SubjectCreateModel
import com.example.neuroed.repository.SubjectCreateRepository
import kotlinx.coroutines.launch

class SubjectCreateViewModel(private val repository: SubjectCreateRepository) : ViewModel() {

    // Mutable state to hold the response after creating a subject.
    var createdSubjectResponse by mutableStateOf<CreatedSubjectResponse?>(null)
        private set

    // Function to create a subject.
    fun createSubject(subjectCreateModel: SubjectCreateModel) {
        viewModelScope.launch {
            try {
                val response = repository.createSubject(subjectCreateModel)
                createdSubjectResponse = response
            } catch (e: Exception) {
                // Handle error (optional)
                e.printStackTrace()
            }
        }
    }
}
