package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.testnotificationmodel
import com.example.neuroed.repository.TestNotificationModelPredicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TestNotificationViewModel(private val repository: TestNotificationModelPredicationRepository) : ViewModel() {
    // Mutable state to hold test notification data
    private val _testNotification = MutableStateFlow<List<testnotificationmodel>>(emptyList())
    val testNotification: StateFlow<List<testnotificationmodel>> = _testNotification

    // Function to fetch test notification data
    fun fetchTestNotification(userId: Int = 1) {
        viewModelScope.launch {
            try {
                val result = repository.testNotificationpredication(userId)
                Log.d("TestNotificationViewModel", "Fetched test notification: $result")
                _testNotification.value = result
            } catch (e: Exception) {
                Log.e("TestNotificationViewModel", "Error fetching test notification", e)
            }
        }
    }
}
