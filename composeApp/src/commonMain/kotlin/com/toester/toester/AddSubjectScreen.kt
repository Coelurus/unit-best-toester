package com.toester.toester

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Subject") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<") // Simplified back button to avoid unresolved icons dependency
                    }
                }
            )
        }
    ) { padding ->
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
                label = { Text("Subject Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("Teacher") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("Subject ID (e.g. math101)") },
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
                                quests = emptyList() // Start with no quests
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && teacher.isNotBlank() && id.isNotBlank()
            ) {
                Text("Save Subject")
            }
        }
    }
}
