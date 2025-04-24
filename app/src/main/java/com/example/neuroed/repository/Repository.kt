package com.example.neuroed.repository

import com.example.neuroed.model.Assignment
import com.example.neuroed.model.AttendanceData
import com.example.neuroed.model.CharacterCreate
import com.example.neuroed.model.CharacterCreateResponse
import com.example.neuroed.model.CharacterGetData
import com.example.neuroed.model.ExamGet
import com.example.neuroed.model.ForgettingItem
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.GenerateSessionResponse
import com.example.neuroed.model.LearningProgress
import com.example.neuroed.model.Meditation
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.model.Saveuserinforesposne
import com.example.neuroed.model.SessionEndRequest
import com.example.neuroed.model.SessionEndResponse
import com.example.neuroed.model.SessionStartRequest
import com.example.neuroed.model.SessionStartResponse
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.TaskGet
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestList
//import com.example.neuroed.model.TestListResponse
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.model.UserProfile
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

//===============================================================================================

class CharacterCreateRepository(private val apiService: ApiService) {
    suspend fun CharacterCreatefun(CharacterCreate: CharacterCreate): CharacterCreateResponse {
        return apiService.CreateCharacter(CharacterCreate)
    }
}

//===============================================================================================

// Update your UserCharacterGet repository class
class UserCharacterGet(private val apiService: ApiService) {
    suspend fun getUserCharacters(userId: Int): List<CharacterGetData> {
        return apiService.CharacterGetResponse(userId)
    }

    // Add this method to match what your ViewModel is calling
    suspend fun CharacterGet(userId: Int): List<CharacterGetData> {
        return apiService.CharacterGetResponse(userId)
    }
}

//===============================================================================================


//==============================================================================================

class TaskListRepository(
    private val apiService: ApiService
) {

    suspend fun getTaskList(userId: Int): List<TaskGet> {
        return apiService.getTask(userId)
    }
}

//==============================================================================================

class ExamListRepository(
    private val apiService: ApiService
){
    suspend fun getExamlist(userId: Int): List<ExamGet>{
        return apiService.getExam(userId)
    }
}

//==============================================================================================

class AssignmentListRepository(
    private val apiService: ApiService
){
    suspend fun getAssignmentlist(userId: Int): List<Assignment>{
        return apiService.getAssignment(userId)
    }
}

//===============================================================================================

class MeditationListRepository(
    private val apiService: ApiService
){
    suspend fun getMeditationlist(userId: Int): List<Meditation>{
        return apiService.getMeditation(userId)
    }
}

class ForgettingCurveRepository(
    private val apiService: ApiService
) {
    /**
     * Fetches the list of topics/subtopics with their forgetting‑curve data
     * for the given user and time scale.
     *
     * @param userId the ID of the user whose data you want
     * @param scale one of "minute", "hour", "day", "week", "month"
     */
    suspend fun getForgettingItems(userId: Int, scale: String): List<ForgettingItem> {
        return apiService.getCurveData(userId, scale)
    }
}
//=============================================================================================

class UserProfileRepository(
    private val apiService: ApiService
) {
    /**
     * Fetches the raw UserProfileResponse from the API and converts
     * it into your domain UserProfile object.
     */
    suspend fun getUserProfile(userId: Int): UserProfile {
        val response = apiService.getUserProfile(userId)
        return response.toUserProfile()
    }
}

//=============================================================================================


// AttendanceRepository.kt
class AttendanceRepository(
    private val apiService: ApiService
) {
    /**
     * Fetches the raw AttendanceDataResponse from GET /profile/{userId}/attendance
     */
    suspend fun getAttendanceData(
        userId: Int,
        month: String,
        year: Int
    ): AttendanceData {
        val response = apiService.getAttendanceData(userId, month, year)
        return response.toAttendanceData()
    }

    /**
     * Calls POST /profile/{userId}/attendance/mark/ to mark today as present
     * and returns the updated AttendanceData.
     */
    suspend fun markAttendance(
        userId: Int,
        iso: String
    ): AttendanceData {
        val response = apiService.markAttendance(userId,iso)
        return response.toAttendanceData()
    }
}

//================================================================================

class LearningProgressRepository(
    private val apiService: ApiService
) {
    /**
     * Fetches raw LearningProgressResponse and converts it into your domain model.
     */
    suspend fun getLearningProgress(userId: Int): LearningProgress {
        val response = apiService.getLearningProgress(userId)
        return response.toLearningProgress()
    }
}



// 2) Repository

class SessionRepository(
    private val api: ApiService
) {
    suspend fun startSession(
        userId: Int,
        sessionKey: String,
        deviceInfo: Map<String, Any> = emptyMap(),
        clientTime: String?,       // ISO_OFFSET_DATE_TIME, e.g. "2025-04-23T06:05:01-05:00"
        clientDate: String?,       // "YYYY-MM-DD", e.g. "2025-04-23"
        clientTimezone: String?    // IANA ID, e.g. "America/New_York"
    ): SessionStartResponse {
        val req = SessionStartRequest(
            session_key     = sessionKey,
            device_info     = deviceInfo,
            client_time     = clientTime,
            client_date     = clientDate,
            client_timezone = clientTimezone
        )
        return api.startSession(userId, req)
    }

    suspend fun endSession(
        userId: Int,
        sessionKey: String,
        clientTime: String? = null
    ): SessionEndResponse {
        val req = SessionEndRequest(
            session_key = sessionKey,
            client_time = clientTime
        )
        return api.endSession(userId, req)
    }
}






