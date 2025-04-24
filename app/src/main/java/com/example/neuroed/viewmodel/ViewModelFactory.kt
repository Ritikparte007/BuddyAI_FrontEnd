package com.example.neuroed.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.TestList
import com.example.neuroed.repository.AssignmentListRepository
import com.example.neuroed.repository.AttendanceRepository
//import com.example.neuroed.repository.CallAgentRepository
import com.example.neuroed.repository.CharacterCreateRepository
import com.example.neuroed.repository.ExamListRepository
import com.example.neuroed.repository.ForgettingCurveRepository
import com.example.neuroed.repository.GenerateSessionRepositry
import com.example.neuroed.repository.LearningProgressRepository
import com.example.neuroed.repository.MeditationListRepository
import com.example.neuroed.repository.PhoneNumberRepository
import com.example.neuroed.repository.SessionRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.repository.TaskListRepository
import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.repository.TestListRepository
import com.example.neuroed.repository.TestQuestionCreateRepository
import com.example.neuroed.repository.UserCharacterGet
import com.example.neuroed.repository.UserProfileRepository
//import com.example.neuroed.repository.TestCreateRepositry
import com.example.neuroed.repository.UserinfosaveRepository
import com.example.neuroed.repository.codeVerificationRepository

class PhoneNumberEmailVerificationViewModelFactory(
    private val repository: PhoneNumberRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhoneNumberEmailVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhoneNumberEmailVerificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class CodeVerificationViewModelFactory(
    private val repository: codeVerificationRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CodeVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CodeVerificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class UserinfoSaveViewModelFactory(
    private val repository: UserinfosaveRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CodeVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserInfoSaveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class GenerateSessionMeditationViewModelFactory(
    private val repository: GenerateSessionRepositry
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GenerateSessionMeditationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GenerateSessionMeditationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//========================================== Test Create ViewModel factory=================================

class TestCreateViewModelFactory(
    private val repository: TestCreateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestCreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestCreateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//========================================================================================================

class TestListViewModelFactory(
    private val repository: TestListRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//============================= Test Question List ======================================================================================

class TestQuestionlistViewModelFactory(
    private val repository: TestQuestionCreateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestQuestionListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestQuestionListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


//===============================================================================

class SubjectSyllabusHeadingViewModelFactory(
    private val repository: SubjectSyllabusHeadingRepository,
    private val syllabus_id: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectSyllabusHeadingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectSyllabusHeadingViewModel(repository, syllabus_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//=========================================================================================

class SubjectSyllabusHeadingTopicViewModelFactory(
    private val repository: SubjectSyllabusHeadingTopicRepository,
    private val title_id: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectSyllabusHeadingTopicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectSyllabusHeadingTopicViewModel(repository, title_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//=============================================================================================


class SubjectSyllabusHeadingTopicSubtopicViewModelFactory(
    private val repository: SubjectSyllabusHeadingSubtopicRepository,
    private val topic_id: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectSyllabusHeadingTopicSubtopicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectSyllabusHeadingTopicSubtopicViewModel(repository, topic_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//================================================================================================


class CharacterCreateViewModelFactory(
    private val repository: CharacterCreateRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterCreateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacterCreateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

//==================================================================================================

class CharacterGetViewModelFactory(
    private val repository: UserCharacterGet,
    private val user_id: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserCharacterListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserCharacterListViewModel(repository, user_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


//=================================================================================================



//==================================================================================================

class TaskGetViewModelFactory(
    private val repository: TaskListRepository,
    private val user_id: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(repository, user_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

//==================================================================================================


class ExamGetViewModelFactory(
    private val repository: ExamListRepository,
    private val user_id: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamGetListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExamGetListViewModel(repository, user_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

//==================================================================================================


class assignmentGetViewModelFactory(
    private val repository: AssignmentListRepository,
    private val user_id: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(assignmentGetListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return assignmentGetListViewModel(repository, user_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class MeditationGetViewModelFactory(
    private val repository: MeditationListRepository,
    private val user_id: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom( MeditationGetListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return  MeditationGetListViewModel(repository, user_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}



class ForgettingCurveViewModelFactory(
    private val repository: ForgettingCurveRepository,
    private val user_id: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgettingCurveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgettingCurveViewModel(repository, user_id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

//=======================================================================================


class UserProfileViewModelFactory(
    private val repository: UserProfileRepository,
    private val userId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            return UserProfileViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

//==================================================================================



class AttendanceViewModelFactory(
    private val repository: AttendanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
//=================================================================================


class LearningProgressViewModelFactory(
    private val repository: LearningProgressRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LearningProgressViewModel::class.java)) {
            LearningProgressViewModel(repository, userId) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}



// 4) ViewModelFactory

// SessionViewModelFactory.kt
class SessionViewModelFactory(
    private val repo: SessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

