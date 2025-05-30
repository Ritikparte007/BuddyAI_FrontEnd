package com.example.neuroed.utils

import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun SpeechSynthesizer.speakSsmlSuspend(ssml: String) {
    withContext(Dispatchers.IO) {
        this@speakSsmlSuspend.SpeakSsmlAsync(ssml).get()
    }
}
