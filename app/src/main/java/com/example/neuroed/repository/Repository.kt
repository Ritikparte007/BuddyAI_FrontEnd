package com.example.neuroed.repository

import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.GenerateSessionResponse
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.model.Saveuserinforesposne
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestList
//import com.example.neuroed.model.TestListResponse
import com.example.neuroed.model.TestQuestionList
//import com.example.neuroed.model.TestQuestionListResponse
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.network.ApiService
import com.example.neuroed.network.RetrofitClient.apiService

class codeVerificationRepository(private val apiSerializableLambda: ApiService){
    suspend fun codeverificationfun(codeverification: codeverification): codeverificationresponse{
        return apiService.codeverification(codeverification)
    }
}

class UserinfosaveRepository(private val apiService: ApiService){
    suspend fun userinfosave(Saveuserinfo: Saveuserinfo): Saveuserinforesposne{
        return apiService.userinfosave(Saveuserinfo)
    }
}


class GenerateSessionRepositry(private val apiService: ApiService) {
    suspend fun GenerateSessionfun(generateSession: GenerateSession): GenerateSessionResponse {
        return apiService.GenerateSessionfun(generateSession)
    }
}

//=============================================================================================

class TestCreateRepository(private val apiService: ApiService) {
    suspend fun testCreateFun(testCreate: TestCreate): TestCreateResponse {
        return apiService.testCreate(testCreate.userId, testCreate)
    }
}

//=============================================================================================

class TestListRepository(private val apiService: ApiService) {
    suspend fun fetchTestList(userId: Int): List<TestList> {
        return apiService.getTestList(userId)
    }
}

//================================ Test Create Question list ==============================

class TestQuestionCreateRepository(private val apiService: ApiService) {
    suspend fun fetchTestQuestionList(testQuestionList: TestQuestionList): QuestionResponse {
        return apiService.createTestQuestion(testQuestionList)
    }
}

//========================================================================================

class SubjectSyllabusHeadingRepository(private val apiService: ApiService){
    suspend fun getSubjectsyllabusHeading(syllabus_id: Int): List<SubjectSyllabusHeading>{
        return apiService.getSubjectSyllabusHeading(syllabus_id)
    }
}

class SubjectSyllabusHeadingTopicRepository(private val apiService: ApiService){
    suspend fun getSubjectSyllabusHeadingTopic(title_id: Int): List<SubjectSyllabusHeadingTopic>{
        return apiService.getSubjectSyllabustopic(title_id)
    }
}

class SubjectSyllabusHeadingSubtopicRepository(private val apiService: ApiService){
    suspend fun getSubjectSyllabusHeadingSubtopic( topic_id: Int): List<SubjectSyllabusHeadingTopicSubtopic>{
        return apiService.getSubjectSyllabusSubtopic(topic_id)
    }
}

