            Text("Close")
                    // For many Android versions, WebView cannot render PDF directly via data URL.
                    // The "most reliable" without a library is usually a JS-based viewer like PDF.js
                    // or redirecting to a viewer service.
                    // Here we try to use a simple HTML with an embed/iframe that might work on some versions
                    // or at least show we are trying to render the actual content.
                        // Google Drive Viewer is a common way to display PDFs in WebView
                        // But since we have local bytes, we can try to use a data URL
                        // Note: Large PDFs might fail with data URL.
package com.toester.toester

import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@Composable
actual fun PdfViewer(
    pdfName: String,
    pdfBytes: ByteArray?,
    onClose: () -> Unit,
    onTimeSpent: (seconds: Long) -> Unit,
) {
    var elapsedSeconds by remember { mutableStateOf(0L) }

    // Track whether the app is in the foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            if (isResumed) {
                elapsedSeconds++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (pdfBytes != null) {
            val base64PDF = Base64.encodeToString(pdfBytes, Base64.DEFAULT)
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.allowFileAccess = true
                    }
                },
                update = { webView ->
                    val html = """
                        <html>
                        <body style="margin:0;padding:0;background:black;">
                            <embed src="data:application/pdf;base64,$base64PDF" width="100%" height="100%" type="application/pdf">
                        </body>
                        </html>
                    """.trimIndent()
                    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("PDF data not available for $pdfName", color = Color.White)
            }
        }

        // Top bar with timer and close
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val m = elapsedSeconds / 60
            val s = elapsedSeconds % 60
            Text(
                "⏱ ${if (m > 0) "${m}m ${s}s" else "${s}s"}",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = {
                onTimeSpent(elapsedSeconds)
                onClose()
            }) {
                Text("Close")
            }
        }
    }
}
