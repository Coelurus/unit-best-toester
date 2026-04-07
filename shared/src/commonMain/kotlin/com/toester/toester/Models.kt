package com.toester.toester

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val nickname: String = name,
    val profilePicUrl: String? = null,
    val streakDays: Int,
    val xp: Int,
)

@Serializable
data class Subject(
    val id: String,
    val name: String,
    val teacher: String,
    val quests: List<String>,
    val pdfs: List<String> = emptyList(),
    val pdfData: Map<String, ByteArray> = emptyMap(),
)

@Serializable
data class DailyQuest(
    val subjectName: String,
    val task: String,
    val xpReward: Int,
)

@Serializable
data class Friend(
    val userId: String,
    val name: String,
    val profilePicUrl: String? = null,
    val xp: Int = 0,
    val level: Int = 1,
    val weeklyXp: Int = 0,
    val isYou: Boolean = false,
)

@Serializable
data class FriendRequest(
    val id: String,
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val status: String = "pending", // "pending", "accepted", "declined"
)

@Serializable
data class SendFriendRequestBody(
    val toUserId: String,
)

@Serializable
data class RespondFriendRequestBody(
    val accept: Boolean,
)

@Serializable
data class XpHistoryEntry(
    val day: String,
    val xp: Int,
)

// ---- Level helpers ----

const val XP_PER_LEVEL = 500

fun levelFromXp(xp: Int): Int = (xp / XP_PER_LEVEL) + 1

fun xpProgressInLevel(xp: Int): Int = xp % XP_PER_LEVEL

fun xpNeededForNextLevel(xp: Int): Int = XP_PER_LEVEL - xpProgressInLevel(xp)

