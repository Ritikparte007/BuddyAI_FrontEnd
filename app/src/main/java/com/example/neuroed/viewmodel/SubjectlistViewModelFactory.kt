package com.example.neuroed.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.SubjectlistRepository

class SubjectlistViewModelFactory(
    private val repository: SubjectlistRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectlistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectlistViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
