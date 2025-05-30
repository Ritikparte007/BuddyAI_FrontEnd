package com.example.neuroed.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object AuthUtils {
    /**
     * Firebase से वर्तमान यूजर का ID Token प्राप्त करता है
     * @param callback ID Token या null (यदि फेल हो) के साथ कॉल होता है
     */
    fun getFirebaseToken(callback: (String?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                callback(idToken)
            } else {
                Log.e("Auth", "Error getting Firebase token", task.exception)
                callback(null)
            }
        } ?: callback(null)
    }
}