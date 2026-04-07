package com.toester.toester

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

