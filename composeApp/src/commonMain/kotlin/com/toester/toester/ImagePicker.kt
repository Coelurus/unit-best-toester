package com.toester.toester

import androidx.compose.ui.graphics.ImageBitmap

/** Opens native file picker and returns a data-URI string, or null if cancelled. */
expect suspend fun pickImage(): String?

/** Decodes raw image bytes (PNG / JPEG / …) into a Compose [ImageBitmap]. */
expect fun decodeImageBytes(bytes: ByteArray): ImageBitmap?

