package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.UservisitRepository

class UservisitdataViewModelFactory(private val repository: UservisitRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UservisitdataViewmodel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UservisitdataViewmodel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}