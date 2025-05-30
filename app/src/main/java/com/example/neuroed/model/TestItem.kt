package com.example.neuroed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TestItem(
    val id : Int,
    val user_id : Int,
    val subject: String,
    val difficulty: String,
    val questionCount: Int,
    val coins: Int,
    var SolveQuestion: Int = 0,  // Changed to 'var' so we can update it
    var Completed: Boolean = false,
    val timeLeft: String,
    val note: String,
    val quotes: String,
    val topic_id: Int,
    val topic: String,
    val Subtopic_id: Int,
    val subtopic: String,
    val totalQuestion: Int,
    val endTimeMs: Long,
    val testType:String
) : Parcelable