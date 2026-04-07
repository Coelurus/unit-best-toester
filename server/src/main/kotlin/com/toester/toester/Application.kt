package com.toester.toester

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

// --------------- in-memory data store ---------------

private val profiles = mutableMapOf(
    "alex" to UserProfile(id = "alex", name = "Alex", nickname = "alex_the_great", profilePicUrl = null, streakDays = 9, xp = 1240),
    "dana" to UserProfile(id = "dana", name = "Dana", nickname = "dana_d", profilePicUrl = null, streakDays = 23, xp = 3800),
    "bob"  to UserProfile(id = "bob",  name = "Bob",  nickname = "bobby", profilePicUrl = null, streakDays = 5, xp = 620),
    "eve"  to UserProfile(id = "eve",  name = "Eve",  nickname = "evee", profilePicUrl = null, streakDays = 42, xp = 5100),
)

private val userSubjects = mutableMapOf(
    "alex" to mutableListOf(
        Subject(id = "math", name = "Math", teacher = "Dr. Novak", quests = listOf("Solve 10 integrals", "Review 1 theorem")),
        Subject(id = "physics", name = "Physics", teacher = "Ing. Kral", quests = listOf("Summarize lecture notes", "Complete lab prep")),
        Subject(id = "programming", name = "Programming", teacher = "Mgr. Svoboda", quests = listOf("Implement one algorithm", "Write two unit tests")),
    ),
    "dana" to mutableListOf(
        Subject(id = "chemistry", name = "Chemistry", teacher = "Doc. Hajek", quests = listOf("Write lab report", "Study reaction kinetics")),
        Subject(id = "biology", name = "Biology", teacher = "Dr. Kovar", quests = listOf("Classify 5 specimens", "Review cell division")),
    ),
    "bob" to mutableListOf(
        Subject(id = "math", name = "Math", teacher = "Dr. Novak", quests = listOf("Solve 10 integrals")),
    ),
    "eve" to mutableListOf(
        Subject(id = "ai", name = "AI / ML", teacher = "Prof. Horak", quests = listOf("Train a classifier", "Read paper on transformers")),
    ),
)

// Friends – sets of mutual userId pairs
private val friendships = mutableSetOf(
    setOf("alex", "dana"),
    setOf("alex", "eve"),
    setOf("dana", "bob"),
)

// Friend requests
private var nextRequestId = 1
private val friendRequests = mutableListOf(
    FriendRequest(
        id = "fr0",
        fromUserId = "bob",
        fromUserName = "bobby",
        toUserId = "alex",
        toUserName = "alex_the_great",
        status = "pending",
    ),
)

// XP history (mock last 14 days)
private val xpHistory = mutableMapOf(
    "alex" to listOf(
        XpHistoryEntry("Mar 25", 400),  XpHistoryEntry("Mar 26", 520),
        XpHistoryEntry("Mar 27", 580),  XpHistoryEntry("Mar 28", 640),
        XpHistoryEntry("Mar 29", 640),  XpHistoryEntry("Mar 30", 750),
        XpHistoryEntry("Mar 31", 810),  XpHistoryEntry("Apr 01", 870),
        XpHistoryEntry("Apr 02", 920),  XpHistoryEntry("Apr 03", 1000),
        XpHistoryEntry("Apr 04", 1050), XpHistoryEntry("Apr 05", 1120),
        XpHistoryEntry("Apr 06", 1180), XpHistoryEntry("Apr 07", 1240),
    ),
    "dana" to listOf(
        XpHistoryEntry("Mar 25", 2800), XpHistoryEntry("Mar 26", 2900),
        XpHistoryEntry("Mar 27", 2980), XpHistoryEntry("Mar 28", 3050),
        XpHistoryEntry("Mar 29", 3120), XpHistoryEntry("Mar 30", 3200),
        XpHistoryEntry("Mar 31", 3300), XpHistoryEntry("Apr 01", 3350),
        XpHistoryEntry("Apr 02", 3420), XpHistoryEntry("Apr 03", 3500),
        XpHistoryEntry("Apr 04", 3580), XpHistoryEntry("Apr 05", 3650),
        XpHistoryEntry("Apr 06", 3740), XpHistoryEntry("Apr 07", 3800),
    ),
    "bob" to listOf(
        XpHistoryEntry("Mar 25", 100),  XpHistoryEntry("Mar 26", 150),
        XpHistoryEntry("Mar 27", 200),  XpHistoryEntry("Mar 28", 250),
        XpHistoryEntry("Mar 29", 290),  XpHistoryEntry("Mar 30", 340),
        XpHistoryEntry("Mar 31", 380),  XpHistoryEntry("Apr 01", 410),
        XpHistoryEntry("Apr 02", 440),  XpHistoryEntry("Apr 03", 480),
        XpHistoryEntry("Apr 04", 520),  XpHistoryEntry("Apr 05", 560),
        XpHistoryEntry("Apr 06", 590),  XpHistoryEntry("Apr 07", 620),
    ),
    "eve" to listOf(
        XpHistoryEntry("Mar 25", 4200), XpHistoryEntry("Mar 26", 4300),
        XpHistoryEntry("Mar 27", 4380), XpHistoryEntry("Mar 28", 4450),
        XpHistoryEntry("Mar 29", 4520), XpHistoryEntry("Mar 30", 4600),
        XpHistoryEntry("Mar 31", 4650), XpHistoryEntry("Apr 01", 4720),
        XpHistoryEntry("Apr 02", 4790), XpHistoryEntry("Apr 03", 4850),
        XpHistoryEntry("Apr 04", 4920), XpHistoryEntry("Apr 05", 4980),
        XpHistoryEntry("Apr 06", 5050), XpHistoryEntry("Apr 07", 5100),
    ),
)

// --------------- helpers ---------------

private fun buildDailyQuests(subjects: List<Subject>): List<DailyQuest> {
    return subjects.mapIndexedNotNull { index, subject ->
        subject.quests.firstOrNull()?.let { firstQuest ->
            DailyQuest(
                subjectName = subject.name,
                task = firstQuest,
                xpReward = 40 + (index * 10),
            )
        }
    }
}

private fun profileToFriend(p: UserProfile, isYou: Boolean = false): Friend {
    val history = xpHistory[p.id] ?: emptyList()
    // Weekly XP = XP gained in last 7 entries (representing Mon-Sun)
    val weekly = if (history.size >= 7) {
        history.last().xp - history[history.size - 7].xp
    } else if (history.size >= 2) {
        history.last().xp - history.first().xp
    } else 0

    return Friend(
        userId = p.id,
        name = p.nickname,
        profilePicUrl = p.profilePicUrl,
        xp = p.xp,
        level = levelFromXp(p.xp),
        weeklyXp = weekly,
        isYou = isYou,
    )
}

// --------------- server ---------------

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }

    routing {

        get("/") {
            call.respondText("Toester API is running")
        }

        // ---------- Profile ----------

        route("/api/profile") {
            get("/{userId}") {
                val userId = call.parameters["userId"]!!
                val profile = profiles[userId]
                    ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)
                call.respond(profile)
            }

            put("/{userId}") {
                val userId = call.parameters["userId"]!!
                val updated = call.receive<UserProfile>()
                profiles[userId] = updated.copy(id = userId)
                call.respond(profiles[userId]!!)
            }
        }

        // ---------- Subjects ----------

        route("/api/subjects") {
            get("/{userId}") {
                val userId = call.parameters["userId"]!!
                val subjects = userSubjects[userId]
                    ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)
                call.respond(subjects)
            }
        }

        // ---------- Daily Quests ----------

        route("/api/quests") {
            get("/{userId}") {
                val userId = call.parameters["userId"]!!
                val subjects = userSubjects[userId]
                    ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)
                call.respond(buildDailyQuests(subjects))
            }
        }

        // ---------- Friends (list only – no child routes here!) ----------

        route("/api/friends") {
            get("/{userId}") {
                val userId = call.parameters["userId"]!!
                val myProfile = profiles[userId]
                    ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)

                // Include friends + self for leaderboard
                val friendEntries = friendships
                    .filter { userId in it }
                    .map { pair -> pair.first { it != userId } }
                    .mapNotNull { friendId -> profiles[friendId]?.let { profileToFriend(it) } }

                val selfEntry = profileToFriend(myProfile, isYou = true)
                call.respond(friendEntries + selfEntry)
            }
        }

        // ---------- Friend Requests (send / list / respond) ----------

        route("/api/friend-requests") {
            post("/send/{userId}") {
                val userId = call.parameters["userId"]!!
                val body = call.receive<SendFriendRequestBody>()
                val fromProfile = profiles[userId]
                    ?: return@post call.respondText("User not found", status = HttpStatusCode.NotFound)
                val toProfile = profiles[body.toUserId]
                    ?: return@post call.respondText("Target user not found", status = HttpStatusCode.NotFound)

                val request = FriendRequest(
                    id = "fr${nextRequestId++}",
                    fromUserId = userId,
                    fromUserName = fromProfile.nickname,
                    toUserId = body.toUserId,
                    toUserName = toProfile.nickname,
                    status = "pending",
                )
                friendRequests.add(request)
                call.respond(request)
            }

            get("/incoming/{userId}") {
                val userId = call.parameters["userId"]!!
                call.respond(friendRequests.filter { it.toUserId == userId && it.status == "pending" })
            }

            get("/outgoing/{userId}") {
                val userId = call.parameters["userId"]!!
                call.respond(friendRequests.filter { it.fromUserId == userId && it.status == "pending" })
            }

            put("/{requestId}") {
                val requestId = call.parameters["requestId"]!!
                val body = call.receive<RespondFriendRequestBody>()
                val request = friendRequests.find { it.id == requestId }
                    ?: return@put call.respondText("Request not found", status = HttpStatusCode.NotFound)

                val updated = request.copy(status = if (body.accept) "accepted" else "declined")
                friendRequests[friendRequests.indexOf(request)] = updated

                if (body.accept) {
                    friendships.add(setOf(updated.fromUserId, updated.toUserId))
                }
                call.respond(updated)
            }
        }

        // ---------- XP History ----------

        route("/api/xp-history") {
            get("/{userId}") {
                val userId = call.parameters["userId"]!!
                val history = xpHistory[userId] ?: emptyList()
                call.respond(history)
            }
        }
    }
}