package com.toester.toester

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ToesterApi {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val base get() = getBaseUrl()

    // ---- Profile ----

    suspend fun getProfile(userId: String): UserProfile {
        return client.get("$base/api/profile/$userId").body()
    }

    suspend fun updateProfile(profile: UserProfile): UserProfile {
        return client.put("$base/api/profile/${profile.id}") {
            contentType(ContentType.Application.Json)
            setBody(profile)
        }.body()
    }

    // ---- Subjects ----

    suspend fun getSubjects(userId: String): List<Subject> {
        return client.get("$base/api/subjects/$userId").body()
    }

    // ---- Daily Quests ----

    suspend fun getDailyQuests(userId: String): List<DailyQuest> {
        return client.get("$base/api/quests/$userId").body()
    }
}

