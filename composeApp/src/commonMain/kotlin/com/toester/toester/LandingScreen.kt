package com.toester.toester

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LandingScreen(
    profile: UserProfile,
    dailyQuests: List<DailyQuest>,
    onOpenAccountSettings: () -> Unit,
    onOpenSubjects: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Hi ${profile.name}",
            style = MaterialTheme.typography.headlineMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Streak", style = MaterialTheme.typography.titleMedium)
                    Text("${profile.streakDays} days", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("XP", style = MaterialTheme.typography.titleMedium)
                    Text(profile.xp.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Text(
            text = "Daily quests",
            style = MaterialTheme.typography.titleLarge,
        )

        dailyQuests.forEach { quest ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(quest.subjectName, style = MaterialTheme.typography.titleMedium)
                    Text(quest.task)
                    Text("+${quest.xpReward} XP", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Button(
            onClick = onOpenAccountSettings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Account settings")
        }

        OutlinedButton(
            onClick = onOpenSubjects,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Uni subjects")
        }
    }
}

