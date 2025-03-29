package com.example.neuroed.repository

import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.model.SubjectSyllabusSaveResponse
import com.example.neuroed.network.ApiService

class SubjectSyllabusSaveRepository(private val apiService: ApiService) {
    suspend fun subjectSyllabusSave(subjectSyllabusSaveModel: SubjectSyllabusSaveModel): SubjectSyllabusSaveResponse {
        return apiService.subjectSyllabus(subjectSyllabusSaveModel)
    }
}
