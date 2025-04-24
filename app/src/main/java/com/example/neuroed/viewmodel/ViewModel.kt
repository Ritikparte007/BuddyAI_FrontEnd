package com.example.neuroed.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.GenerateSessionResponse
import com.example.neuroed.model.PhoneNumberVerificationResponse
import com.example.neuroed.model.PhoneNumberVerification
import com.example.neuroed.model.Saveuserinfo
import com.example.neuroed.model.Saveuserinforesposne
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestList
//import com.example.neuroed.model.TestListResponse
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.repository.GenerateSessionRepositry
import com.example.neuroed.repository.PhoneNumberRepository
import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.repository.TestListRepository
//import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.repository.UserinfosaveRepository
import com.example.neuroed.repository.codeVerificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.neuroed.model.Assignment
import com.example.neuroed.model.AttendanceData
import com.example.neuroed.model.CharacterCreate
import com.example.neuroed.model.CharacterCreateResponse
import com.example.neuroed.model.CharacterGetData
import com.example.neuroed.model.ExamGet
import com.example.neuroed.model.ForgettingItem
import com.example.neuroed.model.LearningProgress
import com.example.neuroed.model.Meditation
import com.example.neuroed.model.QuestionItem
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.SessionEndResponse
import com.example.neuroed.model.SessionStartResponse
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.TaskGet
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.model.UserProfile
import com.example.neuroed.repository.AssignmentListRepository
import com.example.neuroed.repository.AttendanceRepository
//import com.example.neuroed.repository.AttendancemarkRepository
//import com.example.neuroed.repository.CallAgentRepository
import com.example.neuroed.repository.CharacterCreateRepository
import com.example.neuroed.repository.ExamListRepository
import com.example.neuroed.repository.ForgettingCurveRepository
import com.example.neuroed.repository.LearningProgressRepository
import com.example.neuroed.repository.MeditationListRepository
import com.example.neuroed.repository.SessionRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
import com.example.neuroed.repository.TaskListRepository
//import com.example.neuroed.model.TestQuestionListResponse
import com.example.neuroed.repository.TestQuestionCreateRepository
import com.example.neuroed.repository.UserCharacterGet
import com.example.neuroed.repository.UserProfileRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import kotlin.math.abs


class PhoneNumberEmailVerificationViewModel(private val repository: PhoneNumberRepository) : ViewModel() {

    // Mutable state to hold the phone number verification response data
    private val _phoneNumberEmailResponse = MutableStateFlow<PhoneNumberVerificationResponse?>(null)
    val phoneNumberEmailResponse: StateFlow<PhoneNumberVerificationResponse?> get() = _phoneNumberEmailResponse

    /**
     * Fetch phone number verification data from the backend.
     *
     * @param model The phone number verification data.
     * @param onSuccess Callback when the backend confirms success.
     * @param onError Callback when an error occurs.
     */
    fun fetchPhoneNumberVerification(
        model: PhoneNumberVerification,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.PhoneNumberVerificationfun(model)
                Log.d("PhoneNumberEmailVM", "Fetched verification data: $result")
                _phoneNumberEmailResponse.value = result

                // Check if the backend returned a success message.
                if (result.message == "successfully") {
                    onSuccess()
                } else {
                    onError("Backend error: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("PhoneNumberEmailVM", "Error fetching verification data", e)
                onError(e.message ?: "An error occurred")
            }
        }
    }
}



class CodeVerificationViewModel(
    private val repository: codeVerificationRepository
) : ViewModel() {

    private val _codeVerificationResponse = MutableStateFlow<codeverificationresponse?>(null)
    val codeVerificationResponse: StateFlow<codeverificationresponse?> get() = _codeVerificationResponse

    fun fetchCodeVerification(
        model: codeverification,
        onSuccess: () -> Unit, // Removed @Composable annotation.
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.codeverificationfun(model)
                Log.d("CodeVerificationVM", "Fetched verification data: $result")
                _codeVerificationResponse.value = result

                // Check if the backend returned a success message.
                if (result.message == "successfully") {
                    onSuccess()
                } else {
                    onError("Backend error: ${result.message}")
                }
            } catch (e: Exception) {
                Log.e("CodeVerificationVM", "Error fetching verification data", e)
                onError(e.message ?: "An error occurred")
            }
        }
    }
}



class UserInfoSaveViewModel(private val repository: UserinfosaveRepository) : ViewModel() {

    private val _userSaveResponse = MutableLiveData<Saveuserinforesposne>()
    val userSaveResponse: LiveData<Saveuserinforesposne> get() = _userSaveResponse

    fun saveUserInfo(model: Saveuserinfo) {
        viewModelScope.launch {
            // Perform the save operation using the repository
            val response = repository.userinfosave(model)
            _userSaveResponse.value = response
        }
    }
}

class GenerateSessionMeditationViewModel(private val repository: GenerateSessionRepositry) : ViewModel() {

    private val _generateSessionResponse = MutableLiveData<GenerateSessionResponse>()
    val generateSessionResponse: LiveData<GenerateSessionResponse> get() = _generateSessionResponse

    fun generateSession(model: GenerateSession) {
        viewModelScope.launch {
            try {
                val response = repository.GenerateSessionfun(model)
                Log.d("GenerateSessionVM", "Response received: $response")
                _generateSessionResponse.value = response
            } catch (e: Exception) {
                Log.e("GenerateSessionVM", "Error generating session: ${e.localizedMessage}", e)
            }
        }
    }

}

//===================================================================================================

class TestCreateViewModel(private val repository: TestCreateRepository) : ViewModel() {

    private val _testCreateResponse = MutableLiveData<TestCreateResponse>()
    val testCreateResponse: LiveData<TestCreateResponse> get() = _testCreateResponse

    fun TestCreateModel(model: TestCreate) {
        viewModelScope.launch {
            try {
                val response = repository.testCreateFun(model)
                Log.d("GenerateSessionVM", "Response received: $response")
                _testCreateResponse.value = response
            } catch (e: Exception) {
                Log.e("GenerateSessionVM", "Error generating session: ${e.localizedMessage}", e)
            }
        }
    }
}
//======================================================================================================

// ViewModel Class
class TestListViewModel(private val repository: TestListRepository) : ViewModel() {

    // Holds the fetched list of TestList items
    private val _testList = mutableStateOf<List<TestList>>(emptyList())
    val testList: State<List<TestList>> = _testList

    fun fetchTestList(userId: Int) {
        viewModelScope.launch {
            try {
                val result = repository.fetchTestList(userId)
                _testList.value = result
            } catch (e: Exception) {
                Log.e("TestListViewModel", "Error fetching test list", e)
                // Optionally, handle errors (e.g. update an error state)
            }
        }
    }
}
//====================================================================================

class TestQuestionListViewModel(private val repository: TestQuestionCreateRepository) : ViewModel() {

    // LiveData for the raw API response.
    private val _rawResponse = MutableLiveData<QuestionResponse>()
    val rawResponse: LiveData<QuestionResponse> = _rawResponse

    // LiveData for the parsed list of question items.
    private val _parsedQuestions = MutableLiveData<List<QuestionItem>>()
    val parsedQuestions: LiveData<List<QuestionItem>> = _parsedQuestions

    fun fetchTestCreateTest(testQuestionList: TestQuestionList) {
        viewModelScope.launch {
            try {
                val result = repository.fetchTestQuestionList(testQuestionList)
                Log.d("TestQuestionListViewModel", "Received raw data: $result")
                _rawResponse.value = result

                // Parse the JSON string as a QuestionResponse object.
                val gson = Gson()
                val parsedData = gson.fromJson(result.questions.toString(), QuestionResponse::class.java)
                _parsedQuestions.value = parsedData.questions

                Log.d("TestQuestionListViewModel", "Parsed data: ${parsedData.questions}")
            } catch (e: Exception) {
                Log.e("TestQuestionListViewModel", "Error fetching test question list", e)
            }
        }
    }
}

//======================================================================================================================

class SubjectSyllabusHeadingViewModel(
    private val repository: SubjectSyllabusHeadingRepository,
    private val syllabus_id: Int
) : ViewModel() {

    private val _subjectsyllabusheading = MutableLiveData<List<SubjectSyllabusHeading>>()
    val subjectSyllabusHeading: LiveData<List<SubjectSyllabusHeading>>
        get() = _subjectsyllabusheading

    init {
        fetchSubjectSyllabusHeading()
    }

    private fun fetchSubjectSyllabusHeading() {
        viewModelScope.launch {
            try {
                val syllabusList = repository.getSubjectsyllabusHeading(syllabus_id)
                Log.d("SubjectHeadingViewModel", "Fetched syllabus data: $syllabusList")
                _subjectsyllabusheading.value = syllabusList
            } catch (e: Exception) {
                Log.e("SubjectHeadingViewModel", "Error fetching syllabus data", e)
            }
        }
    }
}

//===============================================================

class SubjectSyllabusHeadingTopicViewModel(
    private val repository: SubjectSyllabusHeadingTopicRepository,
    private val title_id: Int
): ViewModel(){
    private val _subjectsyllabusheadingTopic = MutableLiveData<List<SubjectSyllabusHeadingTopic>>()
    val subjectsyllabusheadingTopic: LiveData<List<SubjectSyllabusHeadingTopic>>
        get() = _subjectsyllabusheadingTopic

    init {

        fetchSubjectSyllabusHeadingTopic()

    }

    private fun fetchSubjectSyllabusHeadingTopic() {
        viewModelScope.launch {
            try {
                val syllabusList = repository.getSubjectSyllabusHeadingTopic(title_id)
                Log.d("SubjectSyllabusViewModel", "Fetched syllabus data: $syllabusList")
                _subjectsyllabusheadingTopic.value = syllabusList
            } catch (e: Exception) {
                Log.e("SubjectSyllabusViewModel", "Error fetching syllabus data", e)
            }
        }
    }

}

//==============================================================================================

class SubjectSyllabusHeadingTopicSubtopicViewModel(
    private val repository: SubjectSyllabusHeadingSubtopicRepository,
    private val topic_id: Int
): ViewModel(){
    private val _subjectsyllabusheadingTopicSubtopic = MutableLiveData<List<SubjectSyllabusHeadingTopicSubtopic>>()
    val subjectsyllabusheadingTopicSubtopic: LiveData<List<SubjectSyllabusHeadingTopicSubtopic>>
        get() = _subjectsyllabusheadingTopicSubtopic

    init {
        fetchSubjectSyllabusHeadingTopicSubtopic()
    }

    private fun fetchSubjectSyllabusHeadingTopicSubtopic() {
        viewModelScope.launch {
            try {
                val syllabusList = repository.getSubjectSyllabusHeadingSubtopic(topic_id)
                Log.d("SubjectSyllabusViewModel", "Fetched syllabus data: $syllabusList")
                _subjectsyllabusheadingTopicSubtopic.value = syllabusList
            } catch (e: Exception) {
                Log.e("SubjectSyllabusViewModel", "Error fetching syllabus data", e)
            }
        }
    }

}

//==================================================================================================

class CharacterCreateViewModel(
    private val repository: CharacterCreateRepository
) : ViewModel() {

    private val _characterCreateResponse = MutableLiveData<CharacterCreateResponse>()
    val characterCreateResponse: LiveData<CharacterCreateResponse> get() = _characterCreateResponse

    fun createCharacter(characterCreate: CharacterCreate) {
        viewModelScope.launch {
            try {
                val response = repository.CharacterCreatefun(characterCreate)
                _characterCreateResponse.value = response
            } catch (e: Exception) {
                Log.e("CharacterCreateViewModel", "Error creating character", e)
                // Optionally, handle the error state here.
            }
        }
    }
}


//===================================================================================================



// 2. Fix the ViewModel - there's a typo with the asterisk (*) instead of underscore (_)
class UserCharacterListViewModel(
    private val repository: UserCharacterGet,
    private val user_id: Int,
) : ViewModel() {
    private val _userCharacterList = MutableLiveData<List<CharacterGetData>>()
    val userCharacterList: LiveData<List<CharacterGetData>>
        get() = _userCharacterList

    init {
        fetchUserCharacterList()
    }

    private fun fetchUserCharacterList() {
        viewModelScope.launch {
            try {
                val characterList = repository.CharacterGet(user_id)
                _userCharacterList.value = characterList
                Log.d("UserCharVM", "Fetched characters: $characterList")
            } catch (e: Exception) {
                Log.e("UserCharacterListViewModel", "Error fetching user character list", e)
                _userCharacterList.value = emptyList() // Set empty list on error to avoid null issues
            }
        }
    }
}

//==================================================================================================



//===================================================================================================

class TaskListViewModel(
    private val repository: TaskListRepository,
    private val user_id: Int,
): ViewModel(){

    private val _taskLists = MutableLiveData<List<TaskGet>>()
    val taskList: LiveData<List<TaskGet>>
        get() = _taskLists

    init {
        fetchTaskList()
    }

    private fun fetchTaskList() {
        viewModelScope.launch {
            try {
                val characterList = repository.getTaskList(user_id)
                _taskLists.value = characterList
            } catch (e: Exception) {
                Log.e("UserCharacterListViewModel", "Error fetching user character list", e)
                _taskLists.value = emptyList() // Set empty list on error to avoid null issues
            }
        }
    }
}


//=========================================================================================================


class ExamGetListViewModel(
    private val repository: ExamListRepository,
    private val user_id: Int,
): ViewModel(){

    private val _examlist = MutableLiveData<List<ExamGet>>()
    val examlist: LiveData<List<ExamGet>>
        get() = _examlist

    init {
        fetchExamList()
    }

    private fun fetchExamList() {
        viewModelScope.launch {
            try {
                val characterList = repository.getExamlist(user_id)
                _examlist.value = characterList
            } catch (e: Exception) {
                Log.e("UserCharacterListViewModel", "Error fetching user character list", e)
                _examlist.value = emptyList()
            }
        }
    }
}

//===========================================================================================================

class assignmentGetListViewModel(
    private val repository: AssignmentListRepository,
    private val user_id: Int,
): ViewModel(){

    private val _Assignmentlist = MutableLiveData<List<Assignment>>()
    val Assignmentlist: LiveData<List<Assignment>>
        get() = _Assignmentlist

    init {
        fetchAssignmentlistList()
    }

    private fun fetchAssignmentlistList() {
        viewModelScope.launch {
            try {
                val characterList = repository.getAssignmentlist(user_id)
                _Assignmentlist.value = characterList
            } catch (e: Exception) {
                Log.e("UserCharacterListViewModel", "Error fetching user character list", e)
                _Assignmentlist.value = emptyList()
            }
        }
    }
}

//===================================================================================================

class MeditationGetListViewModel(
    private val repository: MeditationListRepository,
    private val user_id: Int,
): ViewModel(){

    private val _meditationlist = MutableLiveData<List<Meditation>>()
    val Meditationlist: MutableLiveData<List<Meditation>>
        get() = _meditationlist

    init {
        fetchMeditationlistlistList()
    }

    private fun fetchMeditationlistlistList() {
        viewModelScope.launch {
            try {
                val characterList = repository.getMeditationlist(user_id)
                _meditationlist.value = characterList
            } catch (e: Exception) {
                Log.e("UserCharacterListViewModel", "Error fetching user character list", e)
                _meditationlist.value = emptyList()
            }
        }
    }
}

//=======================================================================================

class ForgettingCurveViewModel(
    private val repository: ForgettingCurveRepository,
    private val userId: Int
) : ViewModel() {

    private val _curveItems = MutableLiveData<List<ForgettingItem>>()
    val curveItems: LiveData<List<ForgettingItem>> = _curveItems

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Current time‐scale ("minute","hour","day","week","month")
    private val _scale = MutableLiveData<String>("day")
    val scale: LiveData<String> = _scale

    init {
        // Fetch the default curve data on creation
        fetchCurve()
    }

    /** Call this to re‑fetch when scale changes */
    fun setScale(newScale: String) {
        if (_scale.value == newScale) return
        _scale.value = newScale
        fetchCurve()
    }

    /** Internal fetch function */
    fun fetchCurve() {
        val currentScale = _scale.value ?: "day"
        viewModelScope.launch {
            try {
                _error.value = null
                val items = repository.getForgettingItems(userId, currentScale)
                _curveItems.value = items
            } catch (e: Exception) {
                _curveItems.value = emptyList()
                _error.value = e.localizedMessage
            }
        }
    }
}

//=================================================================================


class UserProfileViewModel(
    private val repository: UserProfileRepository,
    private val userId: Int
) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val profile = repository.getUserProfile(userId)
                Log.d("UserProfileVM", ">>>> fetched profile: $profile")
                _userProfile.value = profile
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error fetching profile", e)
                _userProfile.value = null
            }
        }
    }
}

//===================================================================================================

class AttendanceViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    // Exposed state
    private val _attendance = MutableStateFlow<AttendanceData?>(null)
    val attendance: StateFlow<AttendanceData?> = _attendance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Fetches attendance for a specific month & year.
     */
    fun loadAttendance(userId: Int, month: String, year: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val data = repository.getAttendanceData(userId, month, year)
                _attendance.value = data
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to load attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Marks today as present, sending client-local date/time.
     */
    fun markToday(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Build local ISO-8601 timestamp from Calendar
                val cal    = Calendar.getInstance()
                val year   = cal.get(Calendar.YEAR)
                val month  = cal.get(Calendar.MONTH) + 1       // zero-based
                val day    = cal.get(Calendar.DAY_OF_MONTH)
                val hour   = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                val second = cal.get(Calendar.SECOND)

                // Compute timezone offset hours/minutes
                val tz           = cal.timeZone
                val rawOffset    = tz.rawOffset + if (tz.inDaylightTime(cal.time)) tz.dstSavings else 0
                val offsetHours  = rawOffset / (1000 * 60 * 60)
                val offsetMins   = abs(rawOffset / (1000 * 60) % 60)
                val sign         = if (offsetHours >= 0) '+' else '-'
                val offH         = abs(offsetHours)
                val tzPart       = String.format("%c%02d:%02d", sign, offH, offsetMins)

                // Final ISO string, e.g.: "2025-04-23T14:05:09+05:30"
                val iso = String.format(
                    "%04d-%02d-%02dT%02d:%02d:%02d%s",
                    year, month, day, hour, minute, second, tzPart
                )

                // POST and update state
                val data = repository.markAttendance(userId, iso)
                _attendance.value = data

            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to mark attendance"
            } finally {
                _isLoading.value = false
            }
        }
    }
}


//===================================================================================================


class LearningProgressViewModel(
    private val repository: LearningProgressRepository,
    private val userId: Int
) : ViewModel() {

    private val _learningProgress = MutableLiveData<LearningProgress?>()
    val learningProgress: LiveData<LearningProgress?> = _learningProgress

    init {
        fetchLearningProgress()
    }

    private fun fetchLearningProgress() {
        viewModelScope.launch {
            try {
                val progress = repository.getLearningProgress(userId)
                Log.d("LearningProgressVM", ">>>> fetched progress: $progress")
                _learningProgress.value = progress
            } catch (e: Exception) {
                Log.e("LearningProgressVM", "Error fetching progress", e)
                _learningProgress.value = null
            }
        }
    }
}


// 3) ViewModel



// SessionViewModel.kt
class SessionViewModel(
    private val repo: SessionRepository
) : ViewModel() {

    private val _startResult = MutableLiveData<SessionStartResponse?>()
    val startResult: LiveData<SessionStartResponse?> = _startResult

    private val _endResult = MutableLiveData<SessionEndResponse?>()
    val endResult: LiveData<SessionEndResponse?> = _endResult


    private val tag = "SessionViewModel"

    /**
     * Call this when your app comes to foreground.
     * Pass the client-side timestamp, date, and timezone.
     */
    fun startSession(
        userId: Int,
        sessionKey: String,
        deviceInfo: Map<String, Any> = emptyMap(),
        clientTime: String? = null,
        clientDate: String? = null,
        clientTimezone: String? = null
    ) {
        viewModelScope.launch {
            try {
                val result = repo.startSession(
                    userId         = userId,
                    sessionKey     = sessionKey,
                    deviceInfo     = deviceInfo,
                    clientTime     = clientTime,
                    clientDate     = clientDate,
                    clientTimezone = clientTimezone
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Call this when your app goes to background or onDispose.
     * Pass the client-side timestamp if you have it.
     */
    fun endSession(
        userId: Int,
        sessionKey: String,
        clientTime: String? = null
    ) {
        viewModelScope.launch {
            _endResult.value = try {
                repo.endSession(
                    userId     = userId,
                    sessionKey = sessionKey,
                    clientTime = clientTime
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}


