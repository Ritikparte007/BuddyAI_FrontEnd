package com.example.neuroed.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neuroed.model.GenerateSession
import com.example.neuroed.model.TestList
import com.example.neuroed.repository.GenerateSessionRepositry
import com.example.neuroed.repository.PhoneNumberRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingSubtopicRepository
import com.example.neuroed.repository.SubjectSyllabusHeadingTopicRepository
import com.example.neuroed.repository.SubjectSyllabusSaveRepository
import com.example.neuroed.repository.TestCreateRepository
import com.example.neuroed.repository.TestListRepository
import com.example.neuroed.repository.TestQuestionCreateRepository
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

