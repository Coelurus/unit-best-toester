package com.toester.toester

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

private enum class Screen {
    Landing,
    AccountSettings,
    UniSubjects,
    SubjectDetail,
}

@Composable
@Preview
fun App() {
    val profile = remember {
        UserProfile(
            name = "Alex",
            streakDays = 9,
            xp = 1240,
        )
    }
    val subjects = remember { sampleSubjects() }
    val dailyQuests = remember(subjects) { buildDailyQuests(subjects) }
    var currentScreen by remember { mutableStateOf(Screen.Landing) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    MaterialTheme {
        when (currentScreen) {
            Screen.Landing -> {
                LandingScreen(
                    profile = profile,
                    dailyQuests = dailyQuests,
                    onOpenAccountSettings = { currentScreen = Screen.AccountSettings },
                    onOpenSubjects = { currentScreen = Screen.UniSubjects },
                )
            }

            Screen.AccountSettings -> {
                AccountSettingsScreen(
                    profile = profile,
                    onBack = { currentScreen = Screen.Landing },
                )
            }

            Screen.UniSubjects -> {
                UniSubjectsScreen(
                    subjects = subjects,
                    onBack = { currentScreen = Screen.Landing },
                    onOpenSubject = { subject ->
                        selectedSubject = subject
                        currentScreen = Screen.SubjectDetail
                    },
                )
            }

            Screen.SubjectDetail -> {
                selectedSubject?.let { subject ->
                    SubjectDetailScreen(
                        subject = subject,
                        onBack = { currentScreen = Screen.UniSubjects },
                    )
                }
            }
        }
    }
}