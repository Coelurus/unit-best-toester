package com.toester.toester

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val streakDays: Int,
    val xp: Int,
)

@Serializable
data class Subject(
    val id: String,
    val name: String,
    val teacher: String,
    val quests: List<String>,
)

@Serializable
data class DailyQuest(
    val subjectName: String,
    val task: String,
    val xpReward: Int,
)

