package com.example.neuroed

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.neuroed.voice.GlobalVoiceManager

class NeuroEdApp : Application(), LifecycleEventObserver { // ADD: LifecycleEventObserver

    companion object {
        const val INVALID_USER_ID = 0  // KEEP AS IS
        private var userInfoId: Int = INVALID_USER_ID

        fun getUserInfoId(): Int = userInfoId

        fun setUserInfoId(id: Int) {
            userInfoId = id
            Log.d("NeuroEdApp", "Set userInfoId to: $id")
        }

        fun clearUserInfoId() {
            userInfoId = INVALID_USER_ID
            Log.d("NeuroEdApp", "Cleared userInfoId")
        }

        fun isLoggedIn(): Boolean = userInfoId > 0
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NeuroEdApp", "Application initialized")

        // ADD THESE LINES:
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        try {
            GlobalVoiceManager.initialize(this)
            Log.d("NeuroEdApp", "✅ GlobalVoiceManager initialized")
        } catch (e: Exception) {
            Log.e("NeuroEdApp", "❌ Error initializing GlobalVoiceManager: ${e.message}")
        }
    }

    // ADD THIS ENTIRE FUNCTION:
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> {
                Log.d("NeuroEdApp", "📱 App foreground - reinit voice")
                try {
                    GlobalVoiceManager.initialize(this)
                } catch (e: Exception) {
                    Log.e("NeuroEdApp", "Error reinit voice: ${e.message}")
                }
            }
            Lifecycle.Event.ON_STOP -> {
                Log.d("NeuroEdApp", "📱 App background - cleanup voice")
                try {
                    GlobalVoiceManager.cleanup()
                } catch (e: Exception) {
                    Log.e("NeuroEdApp", "Error cleanup voice: ${e.message}")
                }
            }
            else -> {}
        }
    }

    // ADD THIS ENTIRE FUNCTION:
    override fun onTerminate() {
        try {
            GlobalVoiceManager.cleanup()
        } catch (e: Exception) {
            Log.e("NeuroEdApp", "Error in termination: ${e.message}")
        }
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onTerminate()
    }
}