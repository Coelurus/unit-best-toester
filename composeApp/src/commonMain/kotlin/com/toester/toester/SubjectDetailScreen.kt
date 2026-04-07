package com.toester.toester

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

@Composable
fun SubjectDetailScreen(
    subject: Subject,
    onBack: () -> Unit,
) {
    val pdfs = remember { mutableStateListOf<String>().apply { addAll(subject.pdfs) } }
    val pdfData = remember { mutableStateMapOf<String, ByteArray>().apply { putAll(subject.pdfData) } }
    var selectedPdfToView by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val pdfLauncher = rememberFilePickerLauncher(
        type = PickerType.File(listOf("pdf")),
        title = "Select PDF Lecture"
    ) { file ->
        file?.let { platformFile ->
            scope.launch {
                val bytes = platformFile.readBytes()
                pdfs.add(platformFile.name)
                pdfData[platformFile.name] = bytes
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(subject.name, style = MaterialTheme.typography.headlineMedium)
        Text("Teacher: ${subject.teacher}")

        HorizontalDivider()

        Text("Subject quests", style = MaterialTheme.typography.titleLarge)

        subject.quests.forEach { quest ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = quest,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        Text("Lectures (PDF)", style = MaterialTheme.typography.titleLarge)

        pdfs.forEach { pdfName ->
            Card(
                onClick = { selectedPdfToView = pdfName },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📄", style = MaterialTheme.typography.titleMedium)
                    Text(text = pdfName, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Button(
            onClick = { pdfLauncher.launch() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add PDF Lecture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to subjects")
        }
    }

    if (selectedPdfToView != null) {
        PdfViewer(
            pdfName = selectedPdfToView!!,
            pdfBytes = pdfData[selectedPdfToView!!],
            onClose = { selectedPdfToView = null }
        )
    }
}

