package com.toester.toester

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
actual fun PdfViewer(
    pdfName: String,
    pdfBytes: ByteArray?,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(24.dp)
        ) {
            Text("PDF Viewer: $pdfName", style = MaterialTheme.typography.headlineSmall)
            
            if (pdfBytes != null) {
                Text("Actual PDF content loaded (bytes).", color = Color.Green)
                Text("\nIn this demo, the app is ready to render this data using platform-specific viewers.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Error: PDF data not found", color = Color.Red)
            }

            Button(
                onClick = onClose,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Back to App")
            }
        }
    }
}
