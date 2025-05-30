package com.example.neuroed.ViewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.AssignmentRepository
import com.example.neuroed.viewmodel.AssignmentListViewModel

class AssignmentListViewModelFactory(
    private val repository: AssignmentRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssignmentListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssignmentListViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}