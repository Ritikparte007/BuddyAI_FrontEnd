package com.example.neuroed.repository

import com.example.neuroed.model.SubjectDeleteResponse
import com.example.neuroed.model.SubjectSyllabusDeleteResponse
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService

open class SubjectSyllabusGetRepository(private val apiService: ApiService) {
    suspend fun getSubjectSyllabusGet(subjectId: Int): List<SubjectSyllabusGetResponse> {
        return ApiHelper.executeWithToken { token ->
            apiService.getSubjectSyllabus(subjectId, token)
        }
    }

    // Method to delete a syllabus unit
    suspend fun deleteSubjectSyllabusUnit(syllabusId: Int): SubjectSyllabusDeleteResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.deleteSubjectSyllabusUnit(syllabusId, token)
        }
    }

    // Method to delete a subject
    suspend fun deleteSubject(subjectId: Int): SubjectDeleteResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.deleteSubject(subjectId, token)
        }
    }
}