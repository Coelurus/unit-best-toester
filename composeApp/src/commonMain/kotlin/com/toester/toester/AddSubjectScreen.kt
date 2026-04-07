package com.toester.toester

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    onAdd: (Subject) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val s = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.addNewSubject) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<") // Simplified back button to avoid unresolved icons dependency
                    }
                }
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { 20 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.subjectName) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text(s.teacherLabel) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it },
                    label = { Text(s.subjectId) },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (name.isNotBlank() && teacher.isNotBlank() && id.isNotBlank()) {
                            onAdd(
                                Subject(
                                    id = id,
                                    name = name,
                                    teacher = teacher,
                                    quests = emptyList(), // Start with no quests
                                    pdfs = emptyList(),
                                    pdfData = emptyMap(),
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && teacher.isNotBlank() && id.isNotBlank()
                ) {
                    Text(s.saveSubject)
                }
            }
        }
    }
}
