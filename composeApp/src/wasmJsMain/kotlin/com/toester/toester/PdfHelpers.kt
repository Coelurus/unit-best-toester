package com.toester.toester

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
(base64) => {
    var binaryString = atob(base64);
    var bytes = new Uint8Array(binaryString.length);
    for (var i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
    }
    var blob = new Blob([bytes], { type: 'application/pdf' });
    var url = URL.createObjectURL(blob);
    window.__pdfTabRef = window.open(url, '_blank');
}
""")
private external fun openPdfInNewTabJs(base64: JsString)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { return (window.__pdfTabRef != null && window.__pdfTabRef.closed === true); }")
private external fun isPdfTabClosedJs(): JsBoolean

@OptIn(ExperimentalWasmJsInterop::class)
actual fun openPdfInNewTab(base64: String) {
    openPdfInNewTabJs(base64.toJsString())
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun isPdfTabClosed(): Boolean {
    return isPdfTabClosedJs().toBoolean()
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
() => {
    try {
        if (window.__pdfTabRef && !window.__pdfTabRef.closed) {
            try { return window.__pdfTabRef.document.hasFocus(); } catch(e) {}
            return document.hidden === true;
        }
    } catch(e) {}
    return false;
}
""")
private external fun isPdfTabFocusedJs(): JsBoolean

@OptIn(ExperimentalWasmJsInterop::class)
actual fun isPdfTabFocused(): Boolean {
    return isPdfTabFocusedJs().toBoolean()
}
