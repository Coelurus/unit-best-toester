package com.toester.toester

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.skia.Image as SkiaImage

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
() => {
    window.__pickedImageData = '';
    var input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = function() {
        var file = input.files[0];
        if (file) {
            var reader = new FileReader();
            reader.onload = function() {
                window.__pickedImageData = reader.result;
            };
            reader.readAsDataURL(file);
        }
    };
    input.click();
}
"""
)
private external fun openFilePicker()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { var r = window.__pickedImageData || ''; if (r.indexOf('data:') === 0) { window.__pickedImageData = ''; } return r; }")
private external fun readPickedImage(): JsString

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun pickImage(): String? {
    openFilePicker()
    return withTimeoutOrNull(60_000L) {
        while (true) {
            delay(250)
            val result = readPickedImage().toString()
            if (result.startsWith("data:")) return@withTimeoutOrNull result
        }
        @Suppress("UNREACHABLE_CODE")
        null
    }
}

actual fun decodeImageBytes(bytes: ByteArray): ImageBitmap? = try {
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
} catch (_: Exception) { null }



