package com.toester.toester

import kotlin.random.Random

// Models (UserProfile, Subject, DailyQuest) are now in shared module.
// This file keeps local helpers only.

fun sampleSubjects(): MutableList<Subject> {
    return mutableListOf(
        Subject(
            id = "math",
            name = "Math",
            teacher = "Dr. Novak",
            quests = listOf("Solve 10 integrals", "Review 1 theorem"),
            pdfs = listOf("integrals_basics.pdf"),
            pdfData = emptyMap(),
        ),
        Subject(
            id = "physics",
            name = "Physics",
            teacher = "Ing. Kral",
            quests = listOf("Summarize lecture notes", "Complete lab prep"),
            pdfs = listOf("mechanics_intro.pdf"),
            pdfData = emptyMap(),
        ),
        Subject(
            id = "programming",
            name = "Programming",
            teacher = "Mgr. Svoboda",
            quests = listOf("Implement one algorithm", "Write two unit tests"),
            pdfs = emptyList(),
            pdfData = emptyMap(),
        ),
    )
}

private val questPool = listOf(
    "Solve 10 problems of %s",
    "Review %s lecture notes",
    "Watch a video about %s",
    "Practice %s for 30 minutes",
    "Summarize %s chapter",
    "Explain %s concept to a friend",
    "Create a mind map for %s",
    "Take a quiz on %s",
    "Read 5 pages of %s textbook",
    "Write a short essay on %s",
    "Flashcard session for %s",
    "Listen to a podcast about %s",
    "Discuss %s with a classmate",
    "Organize %s study materials",
    "Prepare for %s exam",
    "Analyze a case study in %s",
    "Complete %s homework assignment",
    "Find a real-world application of %s",
    "Teach %s to someone else",
    "Set 3 learning goals for %s"
)

fun buildDailyQuests(userId: String, subjects: List<Subject>): List<DailyQuest> {
    // Basic seed based on userId hash.
    // In actual production we would use a more robust multiplatform date provider.
    val seed = userId.hashCode().toLong()
    val selectionRandom = Random(seed)

    val allPossibleQuests = subjects.flatMap { subject ->
        questPool.map { taskTemplate ->
            DailyQuest(
                subjectId = subject.id,
                subjectName = subject.name,
                task = taskTemplate.replace("%s", subject.name),
                xpReward = 0
            )
        }
    }

    if (allPossibleQuests.isEmpty()) return emptyList()

    val selectedQuests = allPossibleQuests.shuffled(selectionRandom).take(5)

    return selectedQuests.map {
        it.copy(xpReward = selectionRandom.nextInt(10, 51))
    }
}

