package com.toester.toester

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubjectDetailScreen(
    subject: Subject,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(subject.name, style = MaterialTheme.typography.headlineMedium)
        Text("Teacher: ${subject.teacher}")

        Text("Subject quests", style = MaterialTheme.typography.titleLarge)

        subject.quests.forEach { quest ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = quest,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to subjects")
        }
    }
}

