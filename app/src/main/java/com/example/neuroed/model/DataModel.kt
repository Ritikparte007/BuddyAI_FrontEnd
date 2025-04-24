package com.example.neuroed.model

import java.time.LocalDate
import java.util.concurrent.TimeUnit

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

//============= save userinfo ===================

data class userinfosave(
    val email: String,
    val name: String,
    val photoUrls: String
)

//================== Agent create =================

data class AgentCreate(
    val UserPrompt: String
)

data class AgentCreateResponse(
    val response: String
)


//Create Character ===================================

    data class CharacterCreate(
        val UserId: Int,
        val name: String,
        val instruction1: String,
        val instruction2: String,
        val firstMessage: String,
        val memory: Boolean,
        val isPrivate: Boolean,
    )

    data class CharacterCreateResponse(
        val response: String
    )

//==========================Character get data =======================================

// 1. Fix the data class - this looks fine but ensure it matches your API response
data class CharacterGetData(
    val id: Int,
    val Character_name: String,
    val Description: String,
    val ChatCount: Int,
    val StarCount: Int,
)

//=================================================

data class CallAgent(
    val UserId: Int,
    val time: String,
)

data class CallAgentResponse(
    val response: String
)

//=========================== Task get ===========================

data class TaskGet(
    val userId: Int,
    val taskId: Int,
    val task: String,
    val link: String,
    val progress: String,
    val status: String,
    val date: String,
    val day: String,
    val time: String,
)

//=============================================================

data class ExamGet(
    val userId: Int,
    val id: Int,
    val subject_name: Int,
    val unit: Int,
    val unit_name: String,
    val bad: Int,
    val Average: Int,
    val Good: Int,
    val Excellence: Int,
    val Exam_duration_time: TimeUnit,
)
//==============================================================

data class Assignment(
    val userId: Int,
    val id: Int,
    val subject_name: String,
    val tittle: String,
    val description: String,
    val date: String,
    val status: String,
)

//=================================================================

data class Meditation(
    val userId: Int,
    val id: Int,
    val title: String,
    val description: String,
    val date: String,
)

//===========================================================


// api/Models.kt
data class LearnRequest(
    val retention_score: Float,
    val completed: Boolean,
    val user_id: Int = 1        // hard‑coded for testing
)

data class LearnResponse(
    val next_review_date: String,
    val history: List<HistoryPoint>,
    val curve: List<HistoryPoint>,
    val completed: Boolean
)

data class HistoryPoint(
    val at: String,
    val retention: Float?
)


data class CurvePoint(val at: String, val retention: Float)

data class ForgettingItem(
    val kind: String,
    val id: Int,
    val title: String,
    val unit: String,
    val subject: String,
    val last_review_date: String,
    val next_review_date: String,
    val curve: List<CurvePoint>
)

//======================================================================


data class ProfileData(
    val user: UserProfile,
    val learningProgress: LearningProgress,
    val dailyStats: DailyStats,
    val attendanceData: AttendanceData,
    val monthlyProgress: List<MonthlyProgressData>,
    val achievements: List<Achievement>,
    val learningBuddies: List<LearningBuddy>,
    val motivationalQuotes: List<String>
)

/**
 * Basic user profile information
 */
data class UserProfile(
    val id: Int,
    val user_name: String,
    val user_email: String,
    val avatarUrl: String,
    val level: Int,
    val currentXp: Int,
    val maxXpForLevel: Int,
    val streak: Int
)

/**
 * Data class representing learning progress across subjects
 */
data class LearningProgress(
    val overallCompletionPercentage: Float, // 0.0f to 1.0f
    val subjectsProgress: List<SubjectProgress>
)

/**
 * Progress data for a specific learning subject
 */
data class SubjectProgress(
    val subjectName: String,
    val completionPercentage: Float, // 0.0f to 1.0f
    val color: Long // Hex color value
)

/**
 * Quick stat metrics for the user
 */
data class DailyStats(
    val streak: Int,
    val todayMinutes: Int,
    val completedActivities: Int,
    val totalActivities: Int,
    val earnedTrophies: Int,
    val totalTrophies: Int
)

/**
 * Attendance data for calendar view
 */
data class AttendanceData(
    val currentMonth: MonthAttendance,
    val previousMonth: MonthAttendance?,
    val currentStreak: Int,
    val longestStreak: Int
)

/**
 * Monthly attendance information
 */
data class MonthAttendance(
    val month: String,
    val year: Int,
    val presentDays: List<Int>,
    val totalDays: Int,
    val currentDay: Int? // null for previous months
)

/**
 * Monthly progress data for charts
 */
data class MonthlyProgressData(
    val month: String,
    val progressPercentage: Float, // 0.0f to 1.0f
    val hoursSpent: Int,
    val isCurrentMonth: Boolean
)

/**
 * Achievement badge information
 */
data class Achievement(
    val id: String,
    val name: String,
    val iconName: String,
    val color: Long, // Hex color value
    val earnedDate: LocalDate,
    val description: String
)

/**
 * Learning buddy information
 */
data class LearningBuddy(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val initials: String,
    val isOnline: Boolean,
    val lastActiveTimestamp: Long
)



data class UserProfileResponse(
    val id: Int,
    val user_name: String,
    val user_email: String,
    val avatar_url: String?,
    val level: Int,
    val current_xp: Int,
    val max_xp_for_level: Int,
    val streak: Int
) {
    fun toUserProfile(): UserProfile = UserProfile(
        id = id,
        user_name = user_name,
        user_email = user_email,
        avatarUrl      = avatar_url.orEmpty(),
        level = level,
        currentXp = current_xp,
        maxXpForLevel = max_xp_for_level,
        streak = streak
    )
}

data class SubjectProgressResponse(
    val subject_name: String,
    val completion_percentage: Float,
    val color: String // Hex color like "#64B5F6"
) {
    fun toSubjectProgress(): SubjectProgress = SubjectProgress(
        subjectName = subject_name,
        completionPercentage = completion_percentage,
        color = color.removePrefix("#").toLong(16) or 0xFF000000 // Convert hex string to color long
    )
}

data class LearningProgressResponse(
    val overall_completion_percentage: Float,
    val subjects_progress: List<SubjectProgressResponse>
) {
    fun toLearningProgress(): LearningProgress = LearningProgress(
        overallCompletionPercentage = overall_completion_percentage,
        subjectsProgress = subjects_progress.map { it.toSubjectProgress() }
    )
}

data class DailyStatsResponse(
    val streak: Int,
    val today_minutes: Int,
    val completed_activities: Int,
    val total_activities: Int,
    val earned_trophies: Int,
    val total_trophies: Int
) {
    fun toDailyStats(): DailyStats = DailyStats(
        streak = streak,
        todayMinutes = today_minutes,
        completedActivities = completed_activities,
        totalActivities = total_activities,
        earnedTrophies = earned_trophies,
        totalTrophies = total_trophies
    )
}

data class MonthAttendanceResponse(
    val month: String,
    val year: Int,
    val present_days: List<Int>,
    val total_days: Int,
    val current_day: Int?
) {
    fun toMonthAttendance(): MonthAttendance = MonthAttendance(
        month = month,
        year = year,
        presentDays = present_days,
        totalDays = total_days,
        currentDay = current_day
    )
}

data class AttendanceDataResponse(
    val current_month: MonthAttendanceResponse,
    val previous_month: MonthAttendanceResponse?,
    val current_streak: Int,
    val longest_streak: Int
) {
    fun toAttendanceData(): AttendanceData = AttendanceData(
        currentMonth = current_month.toMonthAttendance(),
        previousMonth = previous_month?.toMonthAttendance(),
        currentStreak = current_streak,
        longestStreak = longest_streak
    )
}


data class MonthlyProgressItemResponse(
    val month: String,
    val progress_percentage: Float,
    val hours_spent: Int,
    val is_current_month: Boolean
) {
    fun toMonthlyProgressData(): MonthlyProgressData = MonthlyProgressData(
        month = month,
        progressPercentage = progress_percentage,
        hoursSpent = hours_spent,
        isCurrentMonth = is_current_month
    )
}

data class MonthlyProgressResponse(
    val data: List<MonthlyProgressItemResponse>
) {
    fun toMonthlyProgressList(): List<MonthlyProgressData> =
        data.map { it.toMonthlyProgressData() }
}

data class AchievementResponse(
    val id: String,
    val name: String,
    val icon_name: String,
    val color: String, // Hex color
    val earned_date: String, // ISO date format
    val description: String
) {
    fun toAchievement(): Achievement = Achievement(
        id = id,
        name = name,
        iconName = icon_name,
        color = color.removePrefix("#").toLong(16) or 0xFF000000,
        earnedDate = LocalDate.parse(earned_date),
        description = description
    )
}

data class AchievementsResponse(
    val achievements: List<AchievementResponse>
) {
    fun toAchievementsList(): List<Achievement> =
        achievements.map { it.toAchievement() }
}

data class LearningBuddyResponse(
    val id: String,
    val name: String,
    val avatar_url: String?,
    val initials: String,
    val is_online: Boolean,
    val last_active_timestamp: Long
) {
    fun toLearningBuddy(): LearningBuddy = LearningBuddy(
        id = id,
        name = name,
        avatarUrl = avatar_url,
        initials = initials,
        isOnline = is_online,
        lastActiveTimestamp = last_active_timestamp
    )
}

data class LearningBuddiesResponse(
    val buddies: List<LearningBuddyResponse>
) {
    fun toLearningBuddiesList(): List<LearningBuddy> =
        buddies.map { it.toLearningBuddy() }
}

data class MotivationalQuotesResponse(
    val quotes: List<String>
)

data class ProfileDataResponse(
    val user: UserProfileResponse,
    val learning_progress: LearningProgressResponse,
    val daily_stats: DailyStatsResponse,
    val attendance_data: AttendanceDataResponse,
    val monthly_progress: List<MonthlyProgressItemResponse>,
    val achievements: List<AchievementResponse>,
    val learning_buddies: List<LearningBuddyResponse>,
    val motivational_quotes: List<String>
) {
    fun toProfileData(): ProfileData = ProfileData(
        user = user.toUserProfile(),
        learningProgress = learning_progress.toLearningProgress(),
        dailyStats = daily_stats.toDailyStats(),
        attendanceData = attendance_data.toAttendanceData(),
        monthlyProgress = monthly_progress.map { it.toMonthlyProgressData() },
        achievements = achievements.map { it.toAchievement() },
        learningBuddies = learning_buddies.map { it.toLearningBuddy() },
        motivationalQuotes = motivational_quotes
    )
}


// 1) Data models for the request/response bodies

// Request to start a session, अब क्लाइंट‐टाइम, क्लाइंट‐डेट और क्लाइंट‐टाइमज़ोन ऑप्शनल हैं
data class SessionStartRequest(
    val session_key: String,
    val device_info: Map<String, Any> = emptyMap(),
    val client_time: String? = null,       // ISO_OFFSET_DATE_TIME, e.g. "2025-04-23T06:05:01-05:00"
    val client_date: String? = null,       // YYYY-MM-DD, e.g. "2025-04-23"
    val client_timezone: String? = null    // IANA ID, e.g. "America/New_York"
)

// Response when session starts, सर्वर और क्लाइंट दोनों के टाइम वापस मिलेंगे
data class SessionStartResponse(
    val session_key: String,
    val start_time: String,                // server timestamp ISO
    val client_time: String?,              // echo back client timestamp
    val client_date: String?,              // echo back client date
    val client_timezone: String?           // echo back client timezone
)

// Request to end a session, क्लाइंट‐टाइम ऑप्शनल
data class SessionEndRequest(
    val session_key: String,
    val client_time: String? = null        // ISO_OFFSET_DATE_TIME
)

// Response when session ends
data class SessionEndResponse(
    val session_key: String,
    val end_time: String,                  // server timestamp ISO
    val client_time: String?               // echo back client timestamp
)



