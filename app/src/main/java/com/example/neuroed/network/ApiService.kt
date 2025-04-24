package com.example.neuroed.network

import com.example.neuroed.model.AchievementsResponse
import com.example.neuroed.model.Assignment
import com.example.neuroed.model.AttendanceDataResponse
import com.example.neuroed.model.CallAgent
import com.example.neuroed.model.CallAgentResponse
import com.example.neuroed.model.CharacterCreate
import com.example.neuroed.model.CharacterCreateResponse
import com.example.neuroed.model.CharacterGetData
import com.example.neuroed.model.CreatedSubjectResponse
import com.example.neuroed.model.DailyStatsResponse
import com.example.neuroed.model.ExamGet
import com.example.neuroed.model.ForgettingItem
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.GenerateSessionResponse
import com.example.neuroed.model.LearnRequest
import com.example.neuroed.model.LearnResponse
import com.example.neuroed.model.LearningBuddiesResponse
import com.example.neuroed.model.LearningProgressResponse
import com.example.neuroed.model.Meditation
import com.example.neuroed.model.MonthlyProgressResponse
import com.example.neuroed.model.MotivationalQuotesResponse
import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.model.PhoneNumberVerificationResponse
import com.example.neuroed.model.ProfileDataResponse
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.model.Saveuserinforesposne
import com.example.neuroed.model.SessionEndRequest
import com.example.neuroed.model.SessionEndResponse
import com.example.neuroed.model.SessionStartRequest
import com.example.neuroed.model.SessionStartResponse
import com.example.neuroed.model.SubjectCreateModel
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.model.SubjectSyllabusSaveResponse
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.model.TaskGet
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestList
//import com.example.neuroed.model.TestListResponse
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.model.UserAppVisitData
import com.example.neuroed.model.UserProfileResponse
import com.example.neuroed.model.Uservisitdatarespponse
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.model.testnotificationmodel
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("Character/save/")
    suspend fun CreateCharacter(@Body characterCreate: CharacterCreate): CharacterCreateResponse

    @GET("api/get/Characterlist/{user_id}/")
    suspend fun CharacterGetResponse(@Path("user_id") userId: Int): List<CharacterGetData>

    @POST("api/call/Agent/{user_id}/")
    suspend fun callAgent(
        @Path("user_id") userId: Int,
        @Body callAgent: CallAgent
    ): CallAgentResponse



    @GET("api/get/Task/{user_id}/")
    suspend fun getTask(@Path("user_id") userId: Int): List<TaskGet>

    @GET("api/get/Task/{user_id}/")
    suspend fun getExam(@Path("user_id") userId: Int): List<ExamGet>

    @GET("api/get/assignment/{user_id}/")
    suspend fun getAssignment(@Path("user_id") userId: Int): List<Assignment>

    @GET("api/get/Meditation/{user_id}/")
    suspend fun getMeditation(@Path("user_id") userId: Int): List<Meditation>


    @POST("api/learn/{kind}/{pk}/")
    suspend fun learnItem(
        @Path("kind") kind: String,       // "topic" or "subtopic"
        @Path("pk") pk: Int,
        @Body req: LearnRequest
    ): LearnResponse

    @GET("api/forgetting-curve-data/{user_id}")
    suspend fun getCurveData(
        @Path("user_id") userId: Int,
        @Query("scale") scale: String  // minute|hour|day|week|month
    ): List<ForgettingItem>

//    =========================================================================================


    @GET("profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: Int): UserProfileResponse

    @GET("api/learning-progress/{user_id}/")
    suspend fun getLearningProgress(
        @Path("user_id") userId: Int
    ): LearningProgressResponse

    @GET("profile/{userId}/daily-stats")
    suspend fun getDailyStats(@Path("userId") userId: String): DailyStatsResponse

    @GET("profile/{userId}/attendance/")
    suspend fun getAttendanceData(
        @Path("userId") userId: Int,
        @Query("month") month: String,
        @Query("year") year: Int
    ): AttendanceDataResponse

    @GET("profile/{userId}/monthly-progress")
    suspend fun getMonthlyProgress(
        @Path("userId") userId: String,
        @Query("months") monthsToFetch: Int = 7
    ): MonthlyProgressResponse

    @GET("profile/{userId}/achievements")
    suspend fun getAchievements(
        @Path("userId") userId: String,
        @Query("limit") limit: Int? = null
    ): AchievementsResponse

    @FormUrlEncoded
    @POST("profile/{userId}/attendance/mark/")
    suspend fun markAttendance(
        @Path("userId") userId: Int,
        @Field("date") dateIso: String
    ): AttendanceDataResponse

    @GET("profile/{userId}/buddies")
    suspend fun getLearningBuddies(@Path("userId") userId: String): LearningBuddiesResponse

    @GET("motivational-quotes")
    suspend fun getMotivationalQuotes(): MotivationalQuotesResponse

    @GET("profile/{userId}/all")
    suspend fun getCompleteProfileData(@Path("userId") userId: String): ProfileDataResponse

    @POST("api/sessions/start/{user_id}/")
    suspend fun startSession(
        @Path("user_id") userId: Int,
        @Body req: SessionStartRequest
    ): SessionStartResponse

    @POST("api/sessions/end/{user_id}/")
    suspend fun endSession(
        @Path("user_id") userId: Int,
        @Body req: SessionEndRequest
    ): SessionEndResponse
}


