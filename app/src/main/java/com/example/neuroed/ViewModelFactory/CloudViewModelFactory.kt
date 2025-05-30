package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.network.ApiService
import com.example.neuroed.repository.CloudRepository

class CloudViewModelFactory(
    private val apiService: ApiService,
    private val userId: Int = 1 // Default user ID, you can change this
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloudViewModel::class.java)) {
            val repository = CloudRepository(apiService)
            @Suppress("UNCHECKED_CAST")
            return CloudViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}