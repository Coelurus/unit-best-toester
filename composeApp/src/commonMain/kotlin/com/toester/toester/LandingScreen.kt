    package com.toester.toester

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LandingScreen(
    profile: UserProfile,
    dailyQuests: List<DailyQuest>,
    onOpenSubjects: () -> Unit,
    onQuestClick: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val s = LocalStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -20 }
        ) {
            Text(
                text = "${s.hi} ${profile.name}",
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { 20 }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(s.streak, style = MaterialTheme.typography.titleMedium)
                        Text("${profile.streakDays} ${s.days}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("XP", style = MaterialTheme.typography.titleMedium)
                        Text(profile.xp.toString(), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, 200))
        ) {
            Text(
                text = s.dailyQuests,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        dailyQuests.forEachIndexed { index, quest ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, 300 + index * 100)) + slideInVertically(tween(600, 300 + index * 100)) { 40 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onQuestClick(quest.subjectId) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(quest.subjectName, style = MaterialTheme.typography.titleMedium)
                        Text(quest.task)
                        Text("+${quest.xpReward} XP", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, 800))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onOpenProfile,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(s.myProfile)
                }

                OutlinedButton(
                    onClick = onOpenSubjects,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(s.uniSubjects)
                }
            }
        }
    }
}

