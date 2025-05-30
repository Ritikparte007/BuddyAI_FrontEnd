package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.network.ApiService

/**
 * Factory for creating LlmExtractViewModel instances
 */
class LlmExtractViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LlmExtractViewModel::class.java)) {
            return LlmExtractViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}