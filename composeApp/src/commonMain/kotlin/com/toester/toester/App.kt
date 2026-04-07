package com.toester.toester

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private enum class Screen {
    Landing,
    UniSubjects,
    SubjectDetail,
    AddSubject,
    Profile,
}

private sealed class LoadState {
    data object Loading : LoadState()
    data class Error(val message: String) : LoadState()
    data object Ready : LoadState()
}

@Composable
@Preview
fun App() {

    val api = remember { ToesterApi() }
    val userId = "alex" // active user id
    val scope = rememberCoroutineScope()

    var loadState by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var subjects by remember { mutableStateOf<MutableList<Subject>>(mutableListOf()) }
    var dailyQuests by remember { mutableStateOf<List<DailyQuest>>(emptyList()) }
    var currentScreen by remember { mutableStateOf(Screen.Landing) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }

    fun loadData() {
        loadState = LoadState.Loading
        scope.launch {
            try {
                profile = api.getProfile(userId)
                subjects = api.getSubjects(userId)
                dailyQuests = api.getDailyQuests(userId)
                loadState = LoadState.Ready
            } catch (e: Exception) {
                // fallback to local sample data so the UI is still usable
                profile = UserProfile(id = userId, name = "Alex", nickname = "alex", streakDays = 0, xp = 0)
                subjects = sampleSubjects()
                dailyQuests = buildDailyQuests(userId, subjects)
                loadState = LoadState.Ready
            }
        }
    }

    LaunchedEffect(userId) { loadData() }

    MaterialTheme {
        AnimatedContent(
            targetState = loadState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { state ->
            when (state) {
                is LoadState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is LoadState.Error -> {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Error: ${state.message}")
                        TextButton(onClick = { loadData() }) { Text("Retry") }
                    }
                }

                is LoadState.Ready -> {
                    val userProfile = profile ?: return@AnimatedContent
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == Screen.Landing) {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                            } else {
                                (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { screen ->
                        when (screen) {
                            Screen.Landing -> {
                                LandingScreen(
                                    profile = userProfile,
                                    dailyQuests = dailyQuests,
                                    onOpenSubjects = { currentScreen = Screen.UniSubjects },
                                    onQuestClick = { subjectId ->
                                        val targetSubject = subjects.find { it.id == subjectId }
                                        if (targetSubject != null) {
                                            selectedSubject = targetSubject
                                            currentScreen = Screen.SubjectDetail
                                        }
                                    },
                                    onOpenProfile = { currentScreen = Screen.Profile },
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
                                    onAddSubject = { currentScreen = Screen.AddSubject }
                                )
                            }

                            Screen.SubjectDetail -> selectedSubject?.let { subject ->
                                SubjectDetailScreen(
                                    subject = subject,
                                    dailyQuests = dailyQuests,
                                    onBack = { currentScreen = Screen.UniSubjects },
                                )
                            }

                            Screen.Profile -> ProfileScreen(
                                profile = userProfile,
                                api = api,
                                onProfileUpdated = { updated ->
                                    profile = updated
                                },
                                onBack = { currentScreen = Screen.Landing },
                            )

                            Screen.AddSubject -> {
                                AddSubjectScreen(
                                    onAdd = { newSubject ->
                                        subjects.add(newSubject)
                                        currentScreen = Screen.UniSubjects
                                    },
                                    onBack = { currentScreen = Screen.UniSubjects }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}