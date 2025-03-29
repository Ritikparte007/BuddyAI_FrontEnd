package com.example.neuroed.model

class codeverification(
    val phoneoremail: String,
    val code: String
)

class codeverificationresponse(
    val message: String,
)


class Saveuserinfo(
    val username: String,
    val phone: String,
    val email: String,
)

class Saveuserinforesposne(
    val message: String
)


data class GenerateSession(
    val userId: Int,
    val Prompt: String,
    val Time: String,
)



data class GenerateSessionResponse(
    val session: String,
)
//==================== Test Create ==========================================================
data class TestCreate(
   val  userId: Int
)

data class TestCreateResponse(
    val response: String
)

//===============================================================================
//========================= List test ==========================================

data class TestList(
    val Subject: String,
    val Difficulty: String,
    val TotalQuestion: Int,
    val TimeCountDown: String,
    val Topic: String,
    val Subtopic: String,
    val Quotes: String
)




//=============================================================================

//=========================== Test Question list ==========================

// Data Models
data class TestQuestionList(
    val subject: String,
    val difficulty: String,
    val totalQuestions: Int,
    val timeCountDown: String,
    val topic: String,
    val subtopic: String
)

data class QuestionItem(
    val question: String,
    val options: List<String>,
    val correct_answer: String
)

data class QuestionResponse(
    val questions: List<QuestionItem>
)

//====================================================================================

data class SubjectSyllabusHeading(
    val id: Int,
    val syllabus: Int,
    val  title: String
)

//====================================

data class SubjectSyllabusHeadingTopic(
    val id: Int,
    val heading: Int,
    val topic: String,
    val  performance_score: Float,
    val  curriculum_importance: Float,
    val  error_rate:Float

)

//===================================

data class SubjectSyllabusHeadingTopicSubtopic(
    val id: Int,
    val topic: Int,
    val subtopic: String,
    val  performance_score: Float,
    val  curriculum_importance: Float,
    val  error_rate:Float
)
