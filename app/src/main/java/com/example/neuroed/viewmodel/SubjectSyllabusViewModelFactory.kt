package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.SubjectSyllabusGetRepository

class SubjectSyllabusViewModelFactory(
    private val repository: SubjectSyllabusGetRepository,
    private val subjectId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectSyllabusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectSyllabusViewModel(repository, subjectId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
