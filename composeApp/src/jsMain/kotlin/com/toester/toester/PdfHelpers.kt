package com.toester.toester

import kotlinx.browser.window

actual fun openPdfInNewTab(base64: String) {
    window.asDynamic().__pdfBase64 = base64
    js("""
    (function() {
        var b64 = window.__pdfBase64;
        var binaryString = atob(b64);
        var bytes = new Uint8Array(binaryString.length);
        for (var i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        var blob = new Blob([bytes], { type: 'application/pdf' });
        var url = URL.createObjectURL(blob);
        window.__pdfTabRef = window.open(url, '_blank');
    })()
    """)
}

actual fun isPdfTabClosed(): Boolean {
    return js("(window.__pdfTabRef != null && window.__pdfTabRef.closed === true)") as Boolean
}

actual fun isPdfTabFocused(): Boolean {
    return js("""
    (function() {
        try {
            if (window.__pdfTabRef && !window.__pdfTabRef.closed) {
                try { return window.__pdfTabRef.document.hasFocus(); } catch(e) {}
                return document.hidden === true;
            }
        } catch(e) {}
        return false;
    })()
    """) as Boolean
}
