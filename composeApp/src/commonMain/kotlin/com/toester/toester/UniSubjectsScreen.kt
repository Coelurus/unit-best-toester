package com.toester.toester

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
fun UniSubjectsScreen(
    subjects: List<Subject>,
    onBack: () -> Unit,
    onOpenSubject: (Subject) -> Unit,
    onAddSubject: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500))
        ) {
            Text("Uni subjects", style = MaterialTheme.typography.headlineMedium)
        }

        subjects.forEachIndexed { index, subject ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, index * 100)) + slideInHorizontally(tween(500, index * 100)) { -40 }
            ) {
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
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500, subjects.size * 100))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onAddSubject, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Subject")
                }

                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to landing")
                }
            }
        }
    }
}

