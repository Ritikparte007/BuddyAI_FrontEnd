package com.example.neuroed.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.repository.TestNotificationModelPredicationRepository

class TestNotificationViewModelFactory(
    private val repository: TestNotificationModelPredicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestNotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestNotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
