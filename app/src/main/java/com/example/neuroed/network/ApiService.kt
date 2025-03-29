package com.example.neuroed.network

import com.example.neuroed.model.CreatedSubjectResponse
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.GenerateSessionResponse
import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.model.PhoneNumberVerificationResponse
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.model.Saveuserinforesposne
import com.example.neuroed.model.SubjectCreateModel
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.model.SubjectSyllabusSaveResponse
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestList
//import com.example.neuroed.model.TestListResponse
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.model.UserAppVisitData
import com.example.neuroed.model.Uservisitdatarespponse
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.model.testnotificationmodel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    // Endpoint for fetching notifications.
    @GET("api/get/Notification/")
    suspend fun getNotifications(): List<NotificationResponse>

    @GET("home_subject/{user_id}/")
    suspend fun getSubjectList(@Path("user_id") userId: Int): List<SubjectlistResponse>

    @GET("api/subject/syllabus/get/{subject_id}")
    suspend fun getSubjectSyllabus(@Path("subject_id") subject_id: Int): List<SubjectSyllabusGetResponse>

    @GET("api/Aistudio/subject/syllabus/heading/{syllabus_id}")
    suspend fun getSubjectSyllabusHeading(@Path("syllabus_id") syllabus_id: Int ): List<SubjectSyllabusHeading>

    @GET("api/Aistudio/subject/syllabus/heading/topic/{title_id}")
    suspend fun getSubjectSyllabustopic(@Path("title_id") title_id: Int): List<SubjectSyllabusHeadingTopic>

     @GET("api/Aistudio/subject/syllabus/heading/topic/subtopic/{topic_id}")
     suspend fun getSubjectSyllabusSubtopic(@Path("topic_id") topic_id: Int): List<SubjectSyllabusHeadingTopicSubtopic>


    // Endpoint for creating a subject.
    @POST("subject/")
    suspend fun subjectCreate(@Body subjectCreateModel: SubjectCreateModel): CreatedSubjectResponse

    // Endpoint for saving syllabus.
    @POST("syllabus/")
    suspend fun subjectSyllabus(@Body subjectSyllabusSaveModel: SubjectSyllabusSaveModel): SubjectSyllabusSaveResponse

    @POST("api/Uservisitdatasave/")
    suspend fun uservisitdatafun(@Body userAppVisitData: UserAppVisitData): Uservisitdatarespponse

    @GET("api/Test/Notification/test/predication/{userid}/")
    suspend fun TestModelpredication(@Path("userid") userid: Int): List<testnotificationmodel>

    @POST("verification-request/")
    suspend fun PhoneNumberVerification(@Body phoneNumberVerification: PhoneNumberVerification): PhoneNumberVerificationResponse

    @POST("api/verify_code/")
    suspend fun codeverification(@Body codeverification: codeverification): codeverificationresponse

    @POST("api/userinfosave/")
    suspend fun userinfosave(@Body Saveuserinfo: Saveuserinfo): Saveuserinforesposne

    @POST("api/GenerateSession/")
    suspend fun GenerateSessionfun(@Body generateSession: GenerateSession): GenerateSessionResponse

    @POST("api/Test/Create/{user_id}/")
     suspend fun testCreate(@Path("user_id") userId: Int, @Body testCreate: TestCreate): TestCreateResponse

    @GET("api/TestCreateGet/{user_id}/")
    suspend fun getTestList(@Path("user_id") userId: Int): List<TestList>

    @POST("api/Test/CreateQuestion/List/")
    suspend fun createTestQuestion(@Body testQuestionList: TestQuestionList): QuestionResponse



}
