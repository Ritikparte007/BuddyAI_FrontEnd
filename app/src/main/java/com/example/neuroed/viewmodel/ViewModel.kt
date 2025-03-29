package com.example.neuroed.viewmodel

import android.util.Log
import androidx.compose.runtime.Composable
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
import com.example.neuroed.model.SubjectSyllabusSaveModel
import com.example.neuroed.model.SubjectSyllabusSaveResponse
import com.example.neuroed.model.TestCreate
import com.example.neuroed.model.TestCreateResponse
import com.example.neuroed.model.TestList
//import com.example.neuroed.model.TestListResponse
import com.example.neuroed.model.codeverification
import com.example.neuroed.model.codeverificationresponse
import com.example.neuroed.repository.GenerateSessionRepositry
import com.example.neuroed.repository.PhoneNumberRepository
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
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
import com.example.neuroed.model.QuestionItem
import com.example.neuroed.model.QuestionResponse
import com.example.neuroed.model.SubjectSyllabusHeading
import com.example.neuroed.model.SubjectSyllabusHeadingTopic
import com.example.neuroed.model.SubjectSyllabusHeadingTopicSubtopic
import com.example.neuroed.model.TestQuestionList
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
//import com.example.neuroed.model.TestQuestionListResponse
import com.example.neuroed.repository.TestQuestionCreateRepository
import com.google.gson.Gson


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
