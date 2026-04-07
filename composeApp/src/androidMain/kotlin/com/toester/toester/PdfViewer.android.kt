package com.toester.toester

import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun PdfViewer(
    pdfName: String,
    pdfBytes: ByteArray?,
    onClose: () -> Unit
) {
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
                        // Google Drive Viewer is a common way to display PDFs in WebView
                        // But since we have local bytes, we can try to use a data URL
                        // Note: Large PDFs might fail with data URL.
                    }
                },
                update = { webView ->
                    // For many Android versions, WebView cannot render PDF directly via data URL.
                    // The "most reliable" without a library is usually a JS-based viewer like PDF.js
                    // or redirecting to a viewer service.
                    // Here we try to use a simple HTML with an embed/iframe that might work on some versions
                    // or at least show we are trying to render the actual content.
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

        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Close")
        }
    }
}
