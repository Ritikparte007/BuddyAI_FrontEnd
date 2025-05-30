package com.example.neuroed.model

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.neuroed.NeuroEdApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserInfoViewModel : ViewModel() {
    private val _userId = MutableStateFlow(NeuroEdApp.INVALID_USER_ID)
    val userId: StateFlow<Int> = _userId.asStateFlow()

    fun loadUserId(context: Context) {
        val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        _userId.value = sharedPrefs.getInt("userInfoId", NeuroEdApp.INVALID_USER_ID)
    }

    // Optional: Add a method to update the userId if needed
    fun updateUserId(newUserId: Int, context: Context) {
        // Update SharedPreferences
        val sharedPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putInt("userInfoId", newUserId).apply()

        // Update the StateFlow
        _userId.value = newUserId
    }
}