package com.toester.toester

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
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
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = 10_000
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

    suspend fun getSubjects(userId: String): MutableList<Subject> {
        return client.get("$base/api/subjects/$userId").body()
    }

    // ---- Daily Quests ----

    suspend fun getDailyQuests(userId: String): List<DailyQuest> {
        return client.get("$base/api/quests/$userId").body()
    }

    // ---- Friends ----

    suspend fun getFriends(userId: String): List<Friend> {
        return client.get("$base/api/friends/$userId").body()
    }

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): FriendRequest {
        return client.post("$base/api/friend-requests/send/$fromUserId") {
            contentType(ContentType.Application.Json)
            setBody(SendFriendRequestBody(toUserId = toUserId))
        }.body()
    }

    suspend fun getIncomingFriendRequests(userId: String): List<FriendRequest> {
        return client.get("$base/api/friend-requests/incoming/$userId").body()
    }

    suspend fun getOutgoingFriendRequests(userId: String): List<FriendRequest> {
        return client.get("$base/api/friend-requests/outgoing/$userId").body()
    }

    suspend fun respondToFriendRequest(requestId: String, accept: Boolean): FriendRequest {
        return client.put("$base/api/friend-requests/$requestId") {
            contentType(ContentType.Application.Json)
            setBody(RespondFriendRequestBody(accept = accept))
        }.body()
    }

    // ---- XP History ----

    suspend fun getXpHistory(userId: String): List<XpHistoryEntry> {
        return client.get("$base/api/xp-history/$userId").body()
    }
}

