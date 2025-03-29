package com.example.neuroed.repository

import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.network.ApiService

open class SubjectSyllabusGetRepository(private val apiService: ApiService) {
    suspend fun getSubjectSyllabusGet(subjectId: Int): List<SubjectSyllabusGetResponse> {
        return apiService.getSubjectSyllabus(subjectId)
    }
}
