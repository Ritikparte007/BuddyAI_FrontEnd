package com.example.neuroed.ViewModelFactory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.MeditationRepository
import com.example.neuroed.viewmodel.MeditationListViewModel

class MeditationListViewModelFactory(
    private val repository: MeditationRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeditationListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MeditationListViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
