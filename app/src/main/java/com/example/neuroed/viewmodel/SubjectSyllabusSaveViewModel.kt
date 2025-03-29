package com.example.neuroed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.model.SubjectSyllabusSaveResponse
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import kotlinx.coroutines.launch

class  SubjectSyllabusSaveViewModel(private val repository: SubjectSyllabusSaveRepository) : ViewModel() {

    private val _saveResponse = MutableLiveData<SubjectSyllabusSaveResponse>()
    val saveResponse: LiveData<SubjectSyllabusSaveResponse> get() = _saveResponse

    fun saveSubjectSyllabus(model: SubjectSyllabusSaveModel) {
        viewModelScope.launch {
            // Perform the save operation using the repository
            val response = repository.subjectSyllabusSave(model)
            _saveResponse.value = response
        }
    }
}
