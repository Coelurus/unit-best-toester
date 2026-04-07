package com.toester.toester

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun PdfViewer(
    pdfName: String,
    pdfBytes: ByteArray?,
    onClose: () -> Unit,
    onTimeSpent: (seconds: Long) -> Unit,
) {
    var pdfTabOpen by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    var tabWasClosed by remember { mutableStateOf(false) }

    // Timer: ticks only while the PDF tab is actually open and focused
    LaunchedEffect(pdfTabOpen) {
        if (!pdfTabOpen) return@LaunchedEffect
        while (true) {
            delay(1_000)
            // Check if the tab was closed by the user
            if (isPdfTabClosed()) {
                pdfTabOpen = false
                tabWasClosed = true
                onTimeSpent(elapsedSeconds)
                break
            }
            // Only count time when the PDF tab is focused
            if (isPdfTabFocused()) {
                elapsedSeconds++
            }
        }
    }

    fun formatTime(totalSeconds: Long): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }

    // Full-screen Compose overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("📄", style = MaterialTheme.typography.displaySmall)

                Text(
                    pdfName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                if (pdfBytes != null) {
                    val base64 = remember(pdfBytes) { Base64.encode(pdfBytes) }

                    if (!pdfTabOpen && !tabWasClosed) {
                        // State 1: Not opened yet
                        Text(
                            "PDF ready (${pdfBytes.size / 1024} KB)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(
                            onClick = {
                                openPdfInNewTab(base64)
                                pdfTabOpen = true
                                elapsedSeconds = 0
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Open PDF in new tab")
                        }
                    } else if (pdfTabOpen) {
                        // State 2: PDF tab is open — show live timer
                        Text(
                            "⏱ Reading…  ${formatTime(elapsedSeconds)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Close the PDF tab to stop the timer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else {
                        // State 3: Tab was closed — show final time
                        Text(
                            "✅ Done reading!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "⏱ ${formatTime(elapsedSeconds)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        // Allow re-opening
                        OutlinedButton(
                            onClick = {
                                openPdfInNewTab(base64)
                                pdfTabOpen = true
                                tabWasClosed = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Open again")
                        }
                    }
                } else {
                    Text(
                        "PDF data not found",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                OutlinedButton(
                    onClick = {
                        if (pdfTabOpen) onTimeSpent(elapsedSeconds)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Close")
                }
            }
        }
    }
}
