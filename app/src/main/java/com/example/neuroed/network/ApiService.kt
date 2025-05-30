package com.example.neuroed.network

import Feed
import com.example.neuroed.model.AchievementsResponse
//import com.example.neuroed.model.ApiResponse
import com.example.neuroed.model.Assignment
import com.example.neuroed.model.Assignmentdata
import com.example.neuroed.model.AttendanceDataResponse
import com.example.neuroed.model.BulkOperationResponse
import com.example.neuroed.model.CallAgent
import com.example.neuroed.model.CallAgentResponse
import com.example.neuroed.model.Challenge
import com.example.neuroed.model.CharacterCreate
import com.example.neuroed.model.CharacterCreateResponse
import com.example.neuroed.model.CharacterGetData
import com.example.neuroed.model.CloudItem
import com.example.neuroed.model.CreateFolderResponse
import com.example.neuroed.model.CreatedSubjectResponse
import com.example.neuroed.model.DailyStatsResponse
import com.example.neuroed.model.DeleteResponse
import com.example.neuroed.model.ExamDto
import com.example.neuroed.model.ExamGet
import com.example.neuroed.model.FirebaseAuthRequest
import com.example.neuroed.model.FirebaseAuthResponse
import com.example.neuroed.model.ForgettingItem
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.GenerateSessionResponse
import com.example.neuroed.model.JoinRequestModel
import com.example.neuroed.model.LearnRequest
import com.example.neuroed.model.LearnResponse
import com.example.neuroed.model.LearningBuddiesResponse
import com.example.neuroed.model.LearningProgressResponse
import com.example.neuroed.model.Meditation
import com.example.neuroed.model.MonthlyProgressResponse
import com.example.neuroed.model.MotivationalQuotesResponse
import com.example.neuroed.model.NetworkResponse
import com.example.neuroed.model.NotificationResponse
import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.model.PhoneNumberVerificationResponse
import com.example.neuroed.model.ProfileDataResponse
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.RenameResponse
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.model.Saveuserinforesposne
import com.example.neuroed.model.SessionEndRequest
import com.example.neuroed.model.SessionEndResponse
import com.example.neuroed.model.SessionStartRequest
import com.example.neuroed.model.SessionStartResponse
import com.example.neuroed.model.ShareResponse
import com.example.neuroed.model.SharedLink
import com.example.neuroed.model.StorageSummary
import com.example.neuroed.model.SubjectCreateModel
import com.example.neuroed.model.SubjectDeleteResponse
import com.example.neuroed.model.SubjectEditModel
import com.example.neuroed.model.SubjectEditResponse
import com.example.neuroed.model.SubjectModel
import com.example.neuroed.model.SubjectSyllabusDeleteResponse
import com.example.neuroed.model.SubjectSyllabusGetResponse
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.model.SubjectSyllabusSaveResponse
import com.example.neuroed.model.SubjectlistResponse
import com.example.neuroed.model.SubmissionResponse
import com.example.neuroed.model.TaskGet
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.model.TestUpdateRequest
import com.example.neuroed.model.TestUpdateResponse
import com.example.neuroed.model.TodayTimeResponse
import com.example.neuroed.model.UploadResponse
import com.example.neuroed.model.UserAppVisitData
//import com.example.neuroed.model.UserInfo
import com.example.neuroed.model.UserProfileResponse
import com.example.neuroed.model.Uservisitdatarespponse
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.model.testnotificationmodel
import com.example.neuroed.repository.ChallengeRequest
import com.example.neuroed.repository.JoinRequestRequest
import com.example.neuroed.repository.StatusUpdateRequest
import okhttp3.MultipartBody
import retrofit2.Response
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Call

interface ApiService {
    // Endpoint for fetching notifications.
    @GET("api/get/Notification/")
    suspend fun getNotifications(
        @Header("Authorization") authHeader: String
    ): List<NotificationResponse>

    @GET("home_subject/{user_id}/")
    suspend fun getSubjectList(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<SubjectlistResponse>

    @GET("api/subject/syllabus/get/{subject_id}")
    suspend fun getSubjectSyllabus(
        @Path("subject_id") subject_id: Int,
        @Header("Authorization") authHeader: String
    ): List<SubjectSyllabusGetResponse>

    @GET("api/Aistudio/subject/syllabus/heading/{syllabus_id}")
    suspend fun getSubjectSyllabusHeading(
        @Path("syllabus_id") syllabus_id: Int,
        @Header("Authorization") authHeader: String
    ): List<SubjectSyllabusHeading>

    @GET("api/Aistudio/subject/syllabus/heading/topic/{title_id}")
    suspend fun getSubjectSyllabustopic(
        @Path("title_id") title_id: Int,
        @Header("Authorization") authHeader: String
    ): List<SubjectSyllabusHeadingTopic>

    @GET("api/Aistudio/subject/syllabus/heading/topic/subtopic/{topic_id}")
    suspend fun getSubjectSyllabusSubtopic(
        @Path("topic_id") topic_id: Int,
        @Header("Authorization") authHeader: String
    ): List<SubjectSyllabusHeadingTopicSubtopic>

    // Endpoint for creating a subject.
    @POST("subject/")
    suspend fun subjectCreate(
        @Body subjectCreateModel: SubjectCreateModel,
        @Header("Authorization") authHeader: String
    ): CreatedSubjectResponse

    // Endpoint for saving syllabus.
    @POST("syllabus/")
    suspend fun subjectSyllabus(
        @Body subjectSyllabusSaveModel: SubjectSyllabusSaveModel,
        @Header("Authorization") authHeader: String
    ): SubjectSyllabusSaveResponse

    @POST("api/Uservisitdatasave/")
    suspend fun uservisitdatafun(
        @Body userAppVisitData: UserAppVisitData,
    ): Uservisitdatarespponse

    @GET("api/Test/Notification/test/predication/{userid}/")
    suspend fun TestModelpredication(
        @Path("userid") userid: Int,
        @Header("Authorization") authHeader: String
    ): List<testnotificationmodel>

    @POST("verification-request/")
    suspend fun PhoneNumberVerification(
        @Body phoneNumberVerification: PhoneNumberVerification,
        @Header("Authorization") authHeader: String
    ): PhoneNumberVerificationResponse

    @POST("api/verify_code/")
    suspend fun codeverification(
        @Body codeverification: codeverification,
        @Header("Authorization") authHeader: String
    ): codeverificationresponse

    @POST("api/userinfosave/")
    suspend fun userinfosave(
        @Body Saveuserinfo: Saveuserinfo,
        @Header("Authorization") authHeader: String
    ): Saveuserinforesposne

    @POST("api/GenerateSession/")
    suspend fun GenerateSessionfun(
        @Body generateSession: GenerateSession,
        @Header("Authorization") authHeader: String
    ): GenerateSessionResponse

    @POST("api/Test/Create/{user_id}/")
    suspend fun testCreate(
        @Path("user_id") userId: Int,
        @Body testCreate: TestCreate,
        @Header("Authorization") authHeader: String
    ): TestCreateResponse

    @GET("api/TestCreateGet/{user_id}/")
    suspend fun getTestList(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<TestDto>

    @POST("api/Test/CreateQuestion/List/")
    suspend fun createTestQuestion(
        @Body testQuestionList: TestQuestionList,
        @Header("Authorization") authHeader: String
    ): QuestionResponse

    @POST("Character/save/")
    suspend fun CreateCharacter(
        @Body characterCreate: CharacterCreate,
        @Header("Authorization") authHeader: String
    ): CharacterCreateResponse

    @GET("api/get/Characterlist/{user_id}/")
    suspend fun CharacterGetResponse(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<CharacterGetData>

    @POST("api/call/Agent/{user_id}/")
    suspend fun callAgent(
        @Path("user_id") userId: Int,
        @Body callAgent: CallAgent,
        @Header("Authorization") authHeader: String
    ): CallAgentResponse

    @GET("api/get/Task/{user_id}/")
    suspend fun getTask(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<TaskGet>

    @GET("api/get/Task/{user_id}/")
    suspend fun getExam(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<ExamGet>

    @GET("api/get/assignment/{user_id}/")
    suspend fun getAssignment(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<Assignment>

    @GET("api/get/Meditation/{user_id}/")
    suspend fun getMeditations(
        @Path("user_id") userId: Int,
        @Query("today") today: Boolean? = null,
        @Header("Authorization") authHeader: String
    ): List<Meditation>

    @POST("api/learn/{kind}/{pk}/")
    suspend fun learnItem(
        @Path("kind") kind: String,       // "topic" or "subtopic"
        @Path("pk") pk: Int,
        @Body req: LearnRequest,
        @Header("Authorization") authHeader: String
    ): LearnResponse

    @GET("api/forgetting-curve-data/{user_id}")
    suspend fun getCurveData(
        @Path("user_id") userId: Int,
        @Query("scale") scale: String,  // minute|hour|day|week|month
        @Header("Authorization") authHeader: String
    ): List<ForgettingItem>

    @GET("profile/{userId}")
    suspend fun getUserProfile(
        @Path("userId") userId: Int,
        @Header("Authorization") authHeader: String
    ): UserProfileResponse

    @GET("api/learning-progress/{user_id}/")
    suspend fun getLearningProgress(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): LearningProgressResponse

    @GET("profile/{userId}/daily-stats")
    suspend fun getDailyStats(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): DailyStatsResponse

    @GET("profile/{userId}/attendance/")
    suspend fun getAttendanceData(
        @Path("userId") userId: Int,
        @Query("month") month: String,
        @Query("year") year: Int,
        @Header("Authorization") authHeader: String
    ): AttendanceDataResponse

    @GET("profile/{userId}/monthly-progress")
    suspend fun getMonthlyProgress(
        @Path("userId") userId: String,
        @Query("months") monthsToFetch: Int = 7,
        @Header("Authorization") authHeader: String
    ): MonthlyProgressResponse

    @GET("profile/{userId}/achievements")
    suspend fun getAchievements(
        @Path("userId") userId: String,
        @Query("limit") limit: Int? = null,
        @Header("Authorization") authHeader: String
    ): AchievementsResponse

    @FormUrlEncoded
    @POST("profile/{userId}/attendance/mark/")
    suspend fun markAttendance(
        @Path("userId") userId: Int,
        @Field("date") dateIso: String,
        @Header("Authorization") authHeader: String
    ): AttendanceDataResponse

    @GET("profile/{userId}/buddies")
    suspend fun getLearningBuddies(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): LearningBuddiesResponse

    @GET("motivational-quotes")
    suspend fun getMotivationalQuotes(
        @Header("Authorization") authHeader: String
    ): MotivationalQuotesResponse

    @GET("profile/{userId}/all")
    suspend fun getCompleteProfileData(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): ProfileDataResponse

    @POST("api/sessions/start/{user_id}/")
    suspend fun startSession(
        @Path("user_id") userId: Int,
        @Body req: SessionStartRequest,
        @Header("Authorization") authHeader: String
    ): SessionStartResponse

    @POST("api/sessions/end/{user_id}/")
    suspend fun endSession(
        @Path("user_id") userId: Int,
        @Body req: SessionEndRequest,
        @Header("Authorization") authHeader: String
    ): SessionEndResponse

    @GET("api/sessions/today/{user_id}/")
    suspend fun getTodayTimeByTz(
        @Path("user_id") userId: Int,
        @Query("tz") tz: String,       // IANA timezone, e.g. "Asia/Kolkata"
        @Header("Authorization") authHeader: String
    ): TodayTimeResponse

    @GET("api/progress/monthly/{user_id}/")
    suspend fun getMonthlyProgress(
        @Path("user_id") userId: Int,
        @Query("months") months: Int = 7,
        @Header("Authorization") authHeader: String
    ): MonthlyProgressResponse

    @GET("api/Assignmentgetdata/{user_id}/")
    suspend fun getAssignments(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<Assignmentdata>

    @Multipart
    @POST("api/assignments/submit/")
    suspend fun submitAssignment(
        @Part("assignment_id") assignmentId: RequestBody,
        @Part("user_id") userId: RequestBody,
        @Part("answer_text") answerText: RequestBody,
        @Part file: MultipartBody.Part?,
        @Header("Authorization") authHeader: String
    ): Response<SubmissionResponse>

    @POST("api/Test/Update/{test_id}/")
    suspend fun updateTestStatus(
        @Path("test_id") testId: Int,
        @Body testUpdateRequest: TestUpdateRequest,
        @Header("Authorization") authHeader: String
    ): TestUpdateResponse

    @GET("api/exams/{user_id}/")
    suspend fun getExams(
        @Path("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<ExamDto>

    @GET("api/exam/{exam_id}/")
    suspend fun getExamById(
        @Path("exam_id") examId: Int,
        @Header("Authorization") authHeader: String
    ): ExamDto

    @GET("feeds/")
    suspend fun getFeeds(
        @Header("Authorization") authHeader: String
    ): Response<List<Feed>>

    @GET("feeds/{id}/")
    suspend fun getFeed(
        @Path("id") id: Int,
        @Header("Authorization") authHeader: String
    ): Response<Feed>

    @POST("feeds/")
    suspend fun createFeed(
        @Body feed: Feed,
        @Header("Authorization") authHeader: String
    ): Response<Feed>

    @PUT("feeds/{id}/")
    suspend fun updateFeed(
        @Path("id") id: Int,
        @Body feed: Feed,
        @Header("Authorization") authHeader: String
    ): Response<Feed>

    @DELETE("feeds/{id}/")
    suspend fun deleteFeed(
        @Path("id") id: Int,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @GET("feeds/by_agent_type/")
    suspend fun getFeedsByAgentType(
        @Query("type") type: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Feed>>

    @GET("feeds/by_content_type/")
    suspend fun getFeedsByContentType(
        @Query("type") type: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Feed>>

    @POST("feeds/{id}/update_time_spent/")
    suspend fun updateTimeSpent(
        @Path("id") id: Int,
        @Body timeSpent: Map<String, Int>,
        @Header("Authorization") authHeader: String
    ): Response<Feed>


    @POST("challenges/")
    suspend fun createChallenge(
        @Body challengeRequest: ChallengeRequest,
        @Header("Authorization") authHeader: String
    ): Response<NetworkResponse<Challenge>>

    @GET("challenges/created/")
    suspend fun getCreatedChallenges(
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): Response<NetworkResponse<List<Challenge>>>

    @GET("challenges/joined/")
    suspend fun getJoinedChallenges(
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): Response<NetworkResponse<List<Challenge>>>

    @GET("join-requests/pending/")
    suspend fun getPendingJoinRequests(
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): Response<NetworkResponse<List<JoinRequestModel>>>

    @PATCH("join-requests/{id}/")
    suspend fun updateJoinRequestStatus(
        @Path("id") requestId: Int,
        @Body statusRequest: StatusUpdateRequest,
        @Header("Authorization") authHeader: String
    ): Response<NetworkResponse<JoinRequestModel>>

    @POST("join-requests/")
    suspend fun createJoinRequest(
        @Body joinRequest: JoinRequestRequest,
        @Header("Authorization") authHeader: String
    ): Response<NetworkResponse<JoinRequestModel>>

    // Authentication endpoints don't need authentication headers
    @POST("api/auth/firebase/")
    fun authenticateWithFirebase(@Body request: FirebaseAuthRequest): Call<FirebaseAuthResponse>

    @POST("api/auth/firebase/get_user/")
    fun getUserByToken(@Body request: FirebaseAuthRequest): Call<FirebaseAuthResponse>

    @Multipart
    @POST("api/extract/document")
    suspend fun extractTextFromDocument(
        @Part file: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<ResponseBody>

    /**
     * Process text with LLM based on a prompt
     * @param requestBody JSON containing text and prompt
     */
    @POST("api/process/text")
    suspend fun processTextWithLlm(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    /**
     * Extract text from an image using OCR + LLM
     * @param image The image file to process
     * @param prompt The instruction prompt for extraction
     */
    @Multipart
    @POST("api/extract/image")
    suspend fun extractTextFromImage(
        @Part image: MultipartBody.Part,
        @Part("prompt") prompt: RequestBody
    ): Response<ResponseBody>


    // NEW: Endpoint for deleting a syllabus unit
    @DELETE("api/subject/syllabus/delete/{syllabus_id}")
    suspend fun deleteSubjectSyllabusUnit(
        @Path("syllabus_id") syllabusId: Int,
        @Header("Authorization") authHeader: String
    ): SubjectSyllabusDeleteResponse

    // NEW: Endpoint for deleting a subject
    @DELETE("api/subject/delete/{subject_id}")
    suspend fun deleteSubject(
        @Path("subject_id") subjectId: Int,
        @Header("Authorization") authHeader: String
    ): SubjectDeleteResponse

    @GET("api/subject/{subject_id}")
    suspend fun getSubjectById(
        @Path("subject_id") subjectId: Int,
        @Header("Authorization") authHeader: String
    ): Response<SubjectModel>
    
    /**
     * Update an existing subject
     */


    @PUT("api/subject/update")
    suspend fun updateSubject(
        @Body subjectEditModel: SubjectEditModel,
        @Header("Authorization") authHeader: String
    ): Response<SubjectEditResponse>

    @GET("cloud/files/")
    suspend fun getCloudFiles(
        @Query("user_id") userId: Int,
        @Query("folder_id") folderId: String? = null,
        @Query("filter") filter: String = "all",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Header("Authorization") authHeader: String
    ): List<CloudItem>

    /**
     * Search cloud files
     */
    @GET("cloud/search/")
    suspend fun searchCloudFiles(
        @Query("user_id") userId: Int,
        @Query("query") query: String,
        @Header("Authorization") authHeader: String
    ): List<CloudItem>

    /**
     * Get recent cloud activity
     */
    @GET("cloud/recent/")
    suspend fun getRecentCloudActivity(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 10,
        @Header("Authorization") authHeader: String
    ): List<CloudItem>

    /**
     * Get storage summary - CORRECTED ENDPOINT
     */
    @GET("cloud/storage-summary/")
    suspend fun getStorageSummary(
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): StorageSummary

    /**
     * Upload file
     */
    @Multipart
    @POST("cloud/upload/")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("user_id") userId: RequestBody,
        @Part("folder_id") folderId: RequestBody? = null,
        @Header("Authorization") authHeader: String
    ): UploadResponse

    /**
     * Create folder - CORRECTED ENDPOINT
     */
    @FormUrlEncoded
    @POST("cloud/create-folder/")
    suspend fun createFolder(
        @Field("user_id") userId: Int,
        @Field("folder_name") folderName: String,
        @Field("parent_folder_id") parentFolderId: String? = null,
        @Header("Authorization") authHeader: String
    ): CreateFolderResponse

    /**
     * Download file - CORRECTED to include user_id
     */
    @GET("cloud/download/{file_id}/")
    suspend fun downloadFile(
        @Path("file_id") fileId: String,
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): ResponseBody

    /**
     * Delete file or folder - CORRECTED to include user_id
     */
    @DELETE("cloud/delete/{file_id}/")
    suspend fun deleteFile(
        @Path("file_id") fileId: String,
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): DeleteResponse

    /**
     * Rename file or folder - CORRECTED to include user_id
     */
    @FormUrlEncoded
    @PUT("cloud/rename/{file_id}/")
    suspend fun renameFile(
        @Path("file_id") fileId: String,
        @Field("user_id") userId: Int,
        @Field("new_name") newName: String,
        @Header("Authorization") authHeader: String
    ): RenameResponse

    /**
     * Share file - CORRECTED to include user_id
     */
    @FormUrlEncoded
    @POST("cloud/share/{file_id}/")
    suspend fun shareFile(
        @Path("file_id") fileId: String,
        @Field("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): ShareResponse

    /**
     * Get file details - CORRECTED to include user_id
     */
    @GET("cloud/file/{file_id}/")
    suspend fun getFileDetails(
        @Path("file_id") fileId: String,
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): CloudItem

    /**
     * Get folder details
     */
    @GET("cloud/folder/{folder_id}/")
    suspend fun getFolderDetails(
        @Path("folder_id") folderId: String,
        @Header("Authorization") authHeader: String
    ): CloudItem

    /**
     * Get folder contents
     */
    @GET("cloud/folder/{folder_id}/contents/")
    suspend fun getFolderContents(
        @Path("folder_id") folderId: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Header("Authorization") authHeader: String
    ): List<CloudItem>

    /**
     * Access shared item
     */
    @GET("cloud/shared/{share_id}/")
    suspend fun accessSharedItem(
        @Path("share_id") shareId: String
    ): CloudItem

    /**
     * Get user's shared links
     */
    @GET("cloud/shared-links/")
    suspend fun getSharedLinks(
        @Query("user_id") userId: Int,
        @Header("Authorization") authHeader: String
    ): List<SharedLink>

    /**
     * Bulk delete files/folders
     */
    @FormUrlEncoded
    @POST("cloud/bulk-delete/")
    suspend fun bulkDelete(
        @Field("user_id") userId: Int,
        @Field("item_ids") itemIds: String, // Comma-separated IDs
        @Header("Authorization") authHeader: String
    ): BulkOperationResponse

    /**
     * Bulk move files/folders
     */
    @FormUrlEncoded
    @POST("cloud/bulk-move/")
    suspend fun bulkMove(
        @Field("user_id") userId: Int,
        @Field("item_ids") itemIds: String, // Comma-separated IDs
        @Field("destination_folder_id") destinationFolderId: String,
        @Header("Authorization") authHeader: String
    ): BulkOperationResponse

}