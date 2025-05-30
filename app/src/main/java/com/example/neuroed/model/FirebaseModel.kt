package com.example.neuroed.model

data class FirebaseAuthRequest(val token: String)

data class FirebaseAuthResponse(
    val userInfoId: Int,
    val username: String?,
    val email: String?
)