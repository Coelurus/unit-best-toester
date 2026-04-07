package com.toester.toester

data class UserProfile(
    val name: String,
    val streakDays: Int,
    val xp: Int,
)

data class Subject(
    val id: String,
    val name: String,
    val teacher: String,
    val quests: List<String>,
)

data class DailyQuest(
    val subjectName: String,
    val task: String,
    val xpReward: Int,
)

fun sampleSubjects(): List<Subject> {
    return listOf(
        Subject(
            id = "math",
            name = "Math",
            teacher = "Dr. Novak",
            quests = listOf("Solve 10 integrals", "Review 1 theorem"),
        ),
        Subject(
            id = "physics",
            name = "Physics",
            teacher = "Ing. Kral",
            quests = listOf("Summarize lecture notes", "Complete lab prep"),
        ),
        Subject(
            id = "programming",
            name = "Programming",
            teacher = "Mgr. Svoboda",
            quests = listOf("Implement one algorithm", "Write two unit tests"),
        ),
    )
}

fun buildDailyQuests(subjects: List<Subject>): List<DailyQuest> {
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

