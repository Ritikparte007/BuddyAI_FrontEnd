package com.example.neuroed.network

import com.example.neuroed.model.TestList
import com.google.gson.annotations.SerializedName


data class TestDto(
    @SerializedName("id")            val id : Int,
    @SerializedName("user_id")       val user_id : Int,
    @SerializedName("Subject")       val Subject: String,
    @SerializedName("Difficulty")    val Difficulty: String,
    @SerializedName("TotalQuestion") val TotalQuestion: Int,
    @SerializedName("SolveQuestion") val SolveQuestion: Int,
    @SerializedName("TimeCountDown") val TimeCountDown: String,
    @SerializedName("topic_id")      val topic_id: Int,
    @SerializedName("Topic")         val Topic: String,
    @SerializedName("Subtopic_id")   val Subtopic_id: Int,
    @SerializedName("Subtopic")      val Subtopic: String,
    @SerializedName("quotes")        val quotes: String,
    @SerializedName("end_time_ms")   val endTimeMs: Long?,
    @SerializedName("test_type")     val test_type: String?
)

fun TestDto.toTestList(): TestList = TestList(
    id            = id,
    user_id       = user_id,
    Subject       = Subject,
    Difficulty    = Difficulty,
    TotalQuestion = TotalQuestion,
    SolveQuestion = SolveQuestion,
    TimeCountDown = TimeCountDown,
    topic_id      = topic_id,
    Topic         = Topic,
    Subtopic_id   = Subtopic_id,
    Subtopic      = Subtopic,
    quotes        = quotes,
    endTimeMs     = endTimeMs ?: 0L,
    test_type     = test_type ?: ""
)