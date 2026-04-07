package com.toester.toester

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.skia.Image as SkiaImage
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume

actual suspend fun pickImage(): String? = suspendCancellableCoroutine { cont ->
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = "image/*"
    input.onchange = {
        val file = input.files?.get(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                cont.resume(reader.result as String)
            }
            reader.readAsDataURL(file)
        } else {
            cont.resume(null)
        }
    }
    input.click()
}

actual fun decodeImageBytes(bytes: ByteArray): ImageBitmap? = try {
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
} catch (_: Exception) { null }

