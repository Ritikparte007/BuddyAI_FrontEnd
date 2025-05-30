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
import com.example.neuroed.model.MonthlyProgressData
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
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.model.UserProfile
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.network.ApiHelper
import com.example.neuroed.network.ApiService
import com.example.neuroed.network.TestDto
import com.example.neuroed.network.toTestList

class codeVerificationRepository(private val apiService: ApiService){
    suspend fun codeverificationfun(codeverification: codeverification): codeverificationresponse{
        return ApiHelper.executeWithToken { token ->
            apiService.codeverification(codeverification, token)
        }
    }
}

class UserinfosaveRepository(private val apiService: ApiService){
    suspend fun userinfosave(Saveuserinfo: Saveuserinfo): Saveuserinforesposne{
        return ApiHelper.executeWithToken { token ->
            apiService.userinfosave(Saveuserinfo, token)
        }
    }
}

class GenerateSessionRepositry(private val apiService: ApiService) {
    suspend fun GenerateSessionfun(generateSession: GenerateSession): GenerateSessionResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.GenerateSessionfun(generateSession, token)
        }
    }
}

class TestCreateRepository(private val apiService: ApiService) {
    suspend fun testCreateFun(testCreate: TestCreate): TestCreateResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.testCreate(testCreate.userId, testCreate, token)
        }
    }
}

class TestListRepository(private val apiService: ApiService) {
    suspend fun fetchTestList(userId: Int): List<TestList> {
        // 1) API से DTOs लाएं
        val dtoList: List<TestDto> = ApiHelper.executeWithToken { token ->
            apiService.getTestList(userId, token)
        }
        // 2) DTOs को Domain मॉडल में मैप करें
        return dtoList.map { it.toTestList() }
    }
}

class TestQuestionCreateRepository(private val apiService: ApiService) {
    suspend fun fetchTestQuestionList(testQuestionList: TestQuestionList): QuestionResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.createTestQuestion(testQuestionList, token)
        }
    }
}

class SubjectSyllabusHeadingRepository(private val apiService: ApiService){
    suspend fun getSubjectsyllabusHeading(syllabus_id: Int): List<SubjectSyllabusHeading>{
        return ApiHelper.executeWithToken { token ->
            apiService.getSubjectSyllabusHeading(syllabus_id, token)
        }
    }
}

class SubjectSyllabusHeadingTopicRepository(private val apiService: ApiService){
    suspend fun getSubjectSyllabusHeadingTopic(title_id: Int): List<SubjectSyllabusHeadingTopic>{
        return ApiHelper.executeWithToken { token ->
            apiService.getSubjectSyllabustopic(title_id, token)
        }
    }
}

class SubjectSyllabusHeadingSubtopicRepository(private val apiService: ApiService){
    suspend fun getSubjectSyllabusHeadingSubtopic(topic_id: Int): List<SubjectSyllabusHeadingTopicSubtopic>{
        return ApiHelper.executeWithToken { token ->
            apiService.getSubjectSyllabusSubtopic(topic_id, token)
        }
    }
}

class CharacterCreateRepository(private val apiService: ApiService) {
    suspend fun CharacterCreatefun(CharacterCreate: CharacterCreate): CharacterCreateResponse {
        return ApiHelper.executeWithToken { token ->
            apiService.CreateCharacter(CharacterCreate, token)
        }
    }
}

// Update your UserCharacterGet repository class
class UserCharacterGet(private val apiService: ApiService) {
    suspend fun getUserCharacters(userId: Int): List<CharacterGetData> {
        return ApiHelper.executeWithToken { token ->
            apiService.CharacterGetResponse(userId, token)
        }
    }

    // Add this method to match what your ViewModel is calling
    suspend fun CharacterGet(userId: Int): List<CharacterGetData> {
        return ApiHelper.executeWithToken { token ->
            apiService.CharacterGetResponse(userId, token)
        }
    }
}

class TaskListRepository(
    private val apiService: ApiService
) {
    suspend fun getTaskList(userId: Int): List<TaskGet> {
        return ApiHelper.executeWithToken { token ->
            apiService.getTask(userId, token)
        }
    }
}

class ExamListRepository(
    private val apiService: ApiService
){
    suspend fun getExamlist(userId: Int): List<ExamGet>{
        return ApiHelper.executeWithToken { token ->
            apiService.getExam(userId, token)
        }
    }
}

class AssignmentListRepository(
    private val apiService: ApiService
){
    suspend fun getAssignmentlist(userId: Int): List<Assignment>{
        return ApiHelper.executeWithToken { token ->
            apiService.getAssignment(userId, token)
        }
    }
}

class MeditationListRepository(
    private val apiService: ApiService
){
    suspend fun getMeditationlist(userId: Int): List<Meditation>{
        return ApiHelper.executeWithToken { token ->
            apiService.getMeditations(userId, null, token)
        }
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
        return ApiHelper.executeWithToken { token ->
            apiService.getCurveData(userId, scale, token)
        }
    }
}

class UserProfileRepository(
    private val apiService: ApiService
) {
    /**
     * Fetches the raw UserProfileResponse from the API and converts
     * it into your domain UserProfile object.
     */
    suspend fun getUserProfile(userId: Int): UserProfile {
        val response = ApiHelper.executeWithToken { token ->
            apiService.getUserProfile(userId, token)
        }
        return response.toUserProfile()
    }
}

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
        val response = ApiHelper.executeWithToken { token ->
            apiService.getAttendanceData(userId, month, year, token)
        }
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
        val response = ApiHelper.executeWithToken { token ->
            apiService.markAttendance(userId, iso, token)
        }
        return response.toAttendanceData()
    }
}

class LearningProgressRepository(
    private val apiService: ApiService
) {
    /**
     * Fetches raw LearningProgressResponse and converts it into your domain model.
     */
    suspend fun getLearningProgress(userId: Int): LearningProgress {
        val response = ApiHelper.executeWithToken { token ->
            apiService.getLearningProgress(userId, token)
        }
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
        return ApiHelper.executeWithToken { token ->
            api.startSession(userId, req, token)
        }
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
        return ApiHelper.executeWithToken { token ->
            api.endSession(userId, req, token)
        }
    }
}

// 3) Repository
// SessionRepository.kt
class SessionTimeRepository(private val api: ApiService) {
    suspend fun fetchTodayMinutes(
        userId: Int,
        tz: String
    ): Int {
        val resp = ApiHelper.executeWithToken { token ->
            api.getTodayTimeByTz(userId, tz, token)
        }
        // convert total seconds → minutes (floor)
        return resp.totalSeconds / 60
    }
}

class MonthlyProgressRepository(
    private val api: ApiService
) {
    suspend fun fetchMonthlyProgress(
        userId: Int,
        months: Int = 7
    ): List<MonthlyProgressData> {
        return ApiHelper.executeWithToken { token ->
            api.getMonthlyProgress(userId, months, token)
        }.toMonthlyProgressList()
    }
}