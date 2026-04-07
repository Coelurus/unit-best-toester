package com.toester.toester

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val systemDark = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDark) }
    val toggleDarkMode = { isDarkMode = !isDarkMode }

    var language by remember { mutableStateOf(AppLanguage.EN) }
    val toggleLanguage = { language = if (language == AppLanguage.EN) AppLanguage.CS else AppLanguage.EN }
    val strings = if (language == AppLanguage.CS) CsStrings else EnStrings

    val toesterDark = darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF3700B3),
        onPrimaryContainer = Color(0xFFE8DEF8),
        secondary = Color(0xFF03DAC6),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF1B3534),
        onSecondaryContainer = Color(0xFFCCF2EE),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF2D2D2D),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        error = Color(0xFFCF6679),
    )

    val toesterLight = lightColorScheme()

    val colorScheme = if (isDarkMode) toesterDark else toesterLight

    CompositionLocalProvider(
        LocalIsDarkMode provides isDarkMode,
        LocalToggleDarkMode provides toggleDarkMode,
        LocalLanguage provides language,
        LocalToggleLanguage provides toggleLanguage,
        LocalStrings provides strings,
    ) {
        MaterialTheme(colorScheme = colorScheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Box(Modifier.fillMaxSize()) {
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
                                    onCompleteQuest = { quest ->
                                        scope.launch {
                                            try {
                                                val updated = api.completeQuest(userId, quest)
                                                profile = updated
                                                dailyQuests = dailyQuests.filter { it.task != quest.task }
                                            } catch (_: Exception) {
                                                profile = profile?.copy(xp = (profile?.xp ?: 0) + quest.xpReward)
                                                dailyQuests = dailyQuests.filter { it.task != quest.task }
                                            }
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
                                    onXpEarned = { xp ->
                                        profile = profile?.copy(xp = (profile?.xp ?: 0) + xp)
                                    },
                                    onCompleteQuest = { quest ->
                                        scope.launch {
                                            try {
                                                val updated = api.completeQuest(userId, quest)
                                                profile = updated
                                                dailyQuests = dailyQuests.filter { it.task != quest.task }
                                            } catch (_: Exception) {
                                                profile = profile?.copy(xp = (profile?.xp ?: 0) + quest.xpReward)
                                                dailyQuests = dailyQuests.filter { it.task != quest.task }
                                            }
                                        }
                                    },
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

                // Floating controls – visible on every screen
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LanguageToggleButton()
                    ThemeToggleButton()
                }
                }
            }
        }
    }
}