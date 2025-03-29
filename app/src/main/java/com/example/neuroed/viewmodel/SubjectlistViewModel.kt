package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.repository.SubjectlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubjectlistViewModel(private val repository: SubjectlistRepository) : ViewModel() {

    // Mutable state to hold subject list data
    private val _subjectList = MutableStateFlow<List<SubjectlistResponse>>(emptyList())
    val subjectList: StateFlow<List<SubjectlistResponse>> = _subjectList

    // Function to fetch subject list data
    fun fetchSubjectList(userId: Int = 1) {
        viewModelScope.launch {
            try {
                val result = repository.getSubjectList(userId)
                Log.d("SubjectlistViewModel", "Fetched subject list: $result")
                _subjectList.value = result
            } catch (e: Exception) {
                Log.e("SubjectlistViewModel", "Error fetching subject list", e)
            }
        }
    }
}
