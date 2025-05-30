package com.example.neuroed.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neuroed.model.Challenge
import com.example.neuroed.model.HostedChallenge
import com.example.neuroed.model.JoinRequestModel
import com.example.neuroed.repository.ChallengeRepository
import kotlinx.coroutines.launch

class ChallengeViewModel(private val repository: ChallengeRepository) : ViewModel() {

    private val _createdChallenges = MutableLiveData<List<HostedChallenge>>(emptyList())
    val createdChallenges: LiveData<List<HostedChallenge>> = _createdChallenges

    private val _joinRequests = MutableLiveData<List<JoinRequestModel>>(emptyList())
    val joinRequests: LiveData<List<JoinRequestModel>> = _joinRequests

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _challengeCreated = MutableLiveData<Boolean>(false)
    val challengeCreated: LiveData<Boolean> = _challengeCreated

    // userId को preferences से प्राप्त करें या दूसरी जगह से
    private var userId: Int = 1  // डिफ़ॉल्ट या वास्तविक उपयोगकर्ता आईडी

    fun setUserId(id: Int) {
        userId = id
    }

    fun loadCreatedChallenges() {
        viewModelScope.launch {
            _loading.value = true

            repository.getCreatedChallenges(userId)  // userId पास करें
                .onSuccess { challenges ->
                    _createdChallenges.value = challenges.map { challenge ->
                        HostedChallenge(
                            id = challenge.id,
                            title = challenge.title,
                            description = challenge.description,
                            topics = challenge.topics,
                            maxParticipants = challenge.maxParticipants,
                            participantCount = challenge.participantCount,
                            difficulty = challenge.difficulty,
                            isPrivate = challenge.isPrivate
                        )
                    }
                    _error.value = null
                }
                .onFailure { error ->
                    _error.value = error.message ?: "Failed to load challenges"
                }

            _loading.value = false
        }
    }

    fun loadJoinRequests() {
        viewModelScope.launch {
            _loading.value = true

            repository.getPendingJoinRequests(userId)  // userId पास करें
                .onSuccess { requests ->
                    _joinRequests.value = requests
                    _error.value = null
                }
                .onFailure { error ->
                    _error.value = error.message ?: "Failed to load join requests"
                }

            _loading.value = false
        }
    }

    fun createChallenge(
        title: String,
        description: String,
        topics: List<String>,
        maxParticipants: Int,
        difficulty: String,
        isPrivate: Boolean,
        userId: Int = this.userId  // userId पैरामीटर जोड़ें, डिफ़ॉल्ट मान के साथ
    ) {
        viewModelScope.launch {
            _loading.value = true

            val challenge = Challenge(
                title = title,
                description = description,
                topics = topics,
                maxParticipants = maxParticipants,
                difficulty = difficulty,
                isPrivate = isPrivate,
                participantCount = 1,
                createdBy = userId  // userId सेट करें
            )

            repository.createChallenge(challenge, userId)  // userId पास करें
                .onSuccess {
                    _challengeCreated.value = true
                    loadCreatedChallenges() // सूची को रिफ्रेश करें
                    _error.value = null
                }
                .onFailure { error ->
                    _error.value = error.message ?: "Failed to create challenge"
                    _challengeCreated.value = false
                }

            _loading.value = false
        }
    }

    fun acceptJoinRequest(requestId: Int) {
        updateJoinRequestStatus(requestId, "accepted")
    }

    fun declineJoinRequest(requestId: Int) {
        updateJoinRequestStatus(requestId, "declined")
    }

    private fun updateJoinRequestStatus(requestId: Int, status: String) {
        viewModelScope.launch {
            _loading.value = true

            repository.updateJoinRequestStatus(requestId, status, userId)  // userId पास करें
                .onSuccess {
                    // अनुरोध को सूची से हटाएं या सूची को रिफ्रेश करें
                    val currentRequests = _joinRequests.value?.toMutableList() ?: mutableListOf()
                    currentRequests.removeAll { it.id == requestId }
                    _joinRequests.value = currentRequests
                    _error.value = null
                }
                .onFailure { error ->
                    _error.value = error.message ?: "Failed to update join request"
                }

            _loading.value = false
        }
    }

    fun resetChallengeCreated() {
        _challengeCreated.value = false
    }

    fun clearError() {
        _error.value = null
    }
}