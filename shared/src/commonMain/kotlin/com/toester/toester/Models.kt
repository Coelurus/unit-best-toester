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
    val pdfs: List<String> = emptyList(),
    val pdfData: Map<String, ByteArray> = emptyMap(),
)

@Serializable
data class DailyQuest(
    val subjectName: String,
    val task: String,
    val xpReward: Int,
)

