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
fun UniSubjectsScreen(
    subjects: List<Subject>,
    onBack: () -> Unit,
    onOpenSubject: (Subject) -> Unit,
    onAddSubject: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Uni subjects", style = MaterialTheme.typography.headlineMedium)

        subjects.forEach { subject ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(subject.name, style = MaterialTheme.typography.titleMedium)
                        Text("Teacher: ${subject.teacher}")
                    }
                    OutlinedButton(onClick = { onOpenSubject(subject) }) {
                        Text("Open")
                    }
                }
            }
        }

        Button(onClick = onAddSubject, modifier = Modifier.fillMaxWidth()) {
            Text("Add Subject")
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to landing")
        }
    }
}

