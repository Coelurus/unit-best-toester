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
    "alex" to UserProfile(id = "alex", name = "Alex", streakDays = 9, xp = 1240),
    "dana" to UserProfile(id = "dana", name = "Dana", streakDays = 23, xp = 3800),
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

        get("/api/profile/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing userId", status = HttpStatusCode.BadRequest)
            val profile = profiles[userId] ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)
            call.respond(profile)
        }

        put("/api/profile/{userId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText("Missing userId", status = HttpStatusCode.BadRequest)
            val updated = call.receive<UserProfile>()
            profiles[userId] = updated.copy(id = userId)
            call.respond(profiles[userId]!!)
        }

        // ---------- Subjects ----------

        get("/api/subjects/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing userId", status = HttpStatusCode.BadRequest)
            val subjects = userSubjects[userId] ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)
            call.respond(subjects)
        }

        // ---------- Daily Quests ----------

        get("/api/quests/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText("Missing userId", status = HttpStatusCode.BadRequest)
            val subjects = userSubjects[userId] ?: return@get call.respondText("User not found", status = HttpStatusCode.NotFound)
            call.respond(buildDailyQuests(subjects))
        }
    }
}