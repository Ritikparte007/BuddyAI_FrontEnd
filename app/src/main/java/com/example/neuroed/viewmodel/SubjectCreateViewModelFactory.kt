package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.SubjectCreateRepository

class SubjectCreateViewModelFactory(private val repository: SubjectCreateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectCreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectCreateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
