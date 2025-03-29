package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    // Mutable state to hold notifications data
    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications

    // Function to fetch notifications
    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                val result = repository.getNotifications()
                Log.d("NotificationViewModel", "Fetched notifications: $result")
                _notifications.value = result
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error fetching notifications", e)
            }
        }
    }
}
