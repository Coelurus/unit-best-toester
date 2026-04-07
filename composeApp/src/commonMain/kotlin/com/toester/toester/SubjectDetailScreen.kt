package com.toester.toester

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.runtime.rememberCoroutineScope
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SubjectDetailScreen(
    subject: Subject,
    dailyQuests: List<DailyQuest>,
    onBack: () -> Unit,
    onXpEarned: (Int) -> Unit = {},
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val s = LocalStrings.current

    val pdfs = remember { mutableStateListOf<String>().apply { addAll(subject.pdfs) } }
    val pdfData = remember { mutableStateMapOf<String, ByteArray>().apply { putAll(subject.pdfData) } }
    val readingTimes = remember { mutableStateMapOf<String, Long>() } // accumulated seconds per PDF
    val awardedMinutes = remember { mutableStateMapOf<String, Long>() } // already-awarded minutes per PDF
    var selectedPdfToView by remember { mutableStateOf<String?>(null) }
    // selected flashcard to view (shows generated flashcards for a PDF)
    var selectedFlashcardToView by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Flashcard states: mapping from PDF name to whether flashcards exist
    val flashcardsExist = remember { mutableStateMapOf<String, Boolean>() }
    // Hover state per PDF flashcard tile
    val flashcardHover = remember { mutableStateMapOf<String, Boolean>() }
    // State to show loading overlay during flashcard generation
    var generatingFlashcardFor by remember { mutableStateOf<String?>(null) }
    var loadingMessage by remember { mutableStateOf("") }

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
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(subject.name, style = MaterialTheme.typography.headlineMedium)
                Text("${s.teacher}: ${subject.teacher}")
                HorizontalDivider()
            }
        }

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(delayMillis = 200))) {
            Text(s.subjectQuests, style = MaterialTheme.typography.titleLarge)
        }

        val subjectQuests = dailyQuests.filter { it.subjectId == subject.id }

        subjectQuests.forEachIndexed { index, quest ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, 300 + index * 100)) + slideInVertically(tween(500, 300 + index * 100)) { 20 }
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = quest.task,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "+${quest.xpReward} XP",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        if (subjectQuests.isEmpty()) {
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(delayMillis = 300))) {
                Text(
                    s.noQuestsToday,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(delayMillis = 500))) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Text(s.lecturesPdf, style = MaterialTheme.typography.titleLarge)
            }
        }

        pdfs.forEach { pdfName ->
            Card(
                onClick = { selectedPdfToView = pdfName },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📄", style = MaterialTheme.typography.titleMedium)
                            Text(text = pdfName, style = MaterialTheme.typography.bodyLarge)
                        }
                        val totalSec = readingTimes[pdfName] ?: 0L
                        if (totalSec > 0) {
                            val m = totalSec / 60
                            val s = totalSec % 60
                            Text(
                                text = if (m > 0) "⏱ ${m}m ${s}s" else "⏱ ${s}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                generatingFlashcardFor = pdfName
                                loadingMessage = "Extracting key concepts from $pdfName..."
                                val seconds = Random.nextInt(2, 6)
                                delay(seconds * 1000L)
                                flashcardsExist[pdfName] = true
                                generatingFlashcardFor = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (flashcardsExist[pdfName] == true) "Regenerate Flash Cards" else "Generate Flash Cards")
                    }
                }
            }

            // Show flash card tile if it exists for this PDF
            if (flashcardsExist[pdfName] == true) {
                // make the flashcard tile clickable to open the flashcard dialog
                val hovered = flashcardHover[pdfName] ?: false
                Card(
                    onClick = { selectedFlashcardToView = pdfName },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp, bottom = 8.dp)
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val ev = awaitPointerEvent()
                                    when (ev.type) {
                                        PointerEventType.Enter -> flashcardHover[pdfName] = true
                                        PointerEventType.Exit -> flashcardHover[pdfName] = false
                                        else -> {}
                                    }
                                }
                            }
                        },
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = if (hovered) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", style = MaterialTheme.typography.titleMedium)
                        Column {
                            Text("Flash Cards: $pdfName", style = MaterialTheme.typography.titleSmall)
                            Text("Practice with generated questions", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(delayMillis = 800))) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { pdfLauncher.launch() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(s.addPdfLecture)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(s.backToSubjects)
                }
            }
        }
    }

    if (selectedPdfToView != null) {
        PdfViewer(
            pdfName = selectedPdfToView!!,
            pdfBytes = pdfData[selectedPdfToView!!],
            onClose = { selectedPdfToView = null },
            onTimeSpent = { seconds ->
                val name = selectedPdfToView ?: return@PdfViewer
                readingTimes[name] = (readingTimes[name] ?: 0L) + seconds
                // Award 1 XP for every full minute of reading
                val totalMinutes = (readingTimes[name] ?: 0L) / 60
                val alreadyAwarded = awardedMinutes[name] ?: 0L
                val newMinutes = totalMinutes - alreadyAwarded
                if (newMinutes > 0) {
                    awardedMinutes[name] = totalMinutes
                    onXpEarned(newMinutes.toInt())
                }
            },
        )
    }

    // Flashcard viewer dialog (shows generated flashcards for a PDF)
    if (selectedFlashcardToView != null) {
        Dialog(onDismissRequest = { selectedFlashcardToView = null }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Flash Cards: ${selectedFlashcardToView}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { selectedFlashcardToView = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Loading overlay for flashcard generation
    if (generatingFlashcardFor != null) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(loadingMessage, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

