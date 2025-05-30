package com.example.neuroed.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// AuthRepository.kt
class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun getIdToken(callback: (String?) -> Unit) {
        val user = getCurrentUser()
        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(task.result?.token)
            } else {
                callback(null)
            }
        } ?: callback(null)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}