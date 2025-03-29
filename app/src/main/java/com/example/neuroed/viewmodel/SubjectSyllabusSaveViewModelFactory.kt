package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.SubjectSyllabusSaveRepository

class SubjectSyllabusSaveViewModelFactory(private val repository: SubjectSyllabusSaveRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectSyllabusSaveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectSyllabusSaveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
