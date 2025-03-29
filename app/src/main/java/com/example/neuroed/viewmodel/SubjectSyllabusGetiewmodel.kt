package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.repository.SubjectSyllabusGetRepository
import kotlinx.coroutines.launch

class SubjectSyllabusViewModel(
    private val repository: SubjectSyllabusGetRepository,
    private val subjectId: Int
) : ViewModel() {

    private val _subjectSyllabus = MutableLiveData<List<SubjectSyllabusGetResponse>>()
    val subjectSyllabus: LiveData<List<SubjectSyllabusGetResponse>>
        get() = _subjectSyllabus

    init {
        fetchSubjectSyllabus()
    }

    private fun fetchSubjectSyllabus() {
        viewModelScope.launch {
            try {
                val syllabusList = repository.getSubjectSyllabusGet(subjectId)
                Log.d("SubjectSyllabusViewModel", "Fetched syllabus data: $syllabusList")
                _subjectSyllabus.value = syllabusList
            } catch (e: Exception) {
                Log.e("SubjectSyllabusViewModel", "Error fetching syllabus data", e)
            }
        }
    }
}
