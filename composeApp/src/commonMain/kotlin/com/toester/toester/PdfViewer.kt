package com.toester.toester

import androidx.compose.runtime.Composable

@Composable
expect fun PdfViewer(
    pdfName: String,
    pdfBytes: ByteArray?,
    onClose: () -> Unit
)
