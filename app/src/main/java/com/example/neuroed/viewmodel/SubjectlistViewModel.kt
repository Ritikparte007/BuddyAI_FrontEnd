package com.example.neuroed.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.neuroed.repository.SubjectlistRepository
import com.example.neuroed.model.SubjectlistResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectlistViewModel(private val repository: SubjectlistRepository) : ViewModel() {

    // Use companion object for shared data across all instances
    companion object {
        // Shared MutableStateFlow to hold the subject list data
        private val _sharedSubjectList = MutableStateFlow<List<SubjectlistResponse>>(emptyList())

        // Flag to track if data is currently being loaded
        private val _isLoading = MutableStateFlow(false)

        // Flag to track if data has been successfully loaded
        private var isDataLoaded = false
    }

    // Expose StateFlow for composables to collect
    val subjectList = _sharedSubjectList.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    /**
     * Fetches the subject list for a given user ID.
     * If data is already loaded or currently loading, it will not fetch again.
     */
    fun fetchSubjectList(userId: Int) {
        // Skip if already loaded or currently loading
        if (isDataLoaded || _isLoading.value) {
            Log.d("SubjectlistViewModel", "Data already loaded or loading in progress. Skipping fetch.")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("SubjectlistViewModel", "Fetching subject list for user: $userId")

                // Fetch data from the API
                val result = repository.getSubjectList(userId)

                // Update the shared state with the result
                _sharedSubjectList.value = result

                // Mark as loaded
                isDataLoaded = true

                Log.d("SubjectlistViewModel", "Successfully loaded ${result.size} subjects")
            } catch (e: Exception) {
                Log.e("SubjectlistViewModel", "Error fetching subject list: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Forces a refresh of the subject list data even if it's already loaded.
     * Use this when you need fresh data.
     */
    fun refreshSubjectList(userId: Int) {
        // Reset loaded flag
        isDataLoaded = false

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("SubjectlistViewModel", "Refreshing subject list for user: $userId")

                // Fetch fresh data
                val result = repository.getSubjectList(userId)

                // Update shared state
                _sharedSubjectList.value = result

                // Mark as loaded
                isDataLoaded = true

                Log.d("SubjectlistViewModel", "Successfully refreshed ${result.size} subjects")
            } catch (e: Exception) {
                Log.e("SubjectlistViewModel", "Error refreshing subject list: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
