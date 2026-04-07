package com.toester.toester

/** Opens a PDF in a new browser tab from base64-encoded data. */
expect fun openPdfInNewTab(base64: String)

/** Returns true if the PDF tab has been closed (or was never opened). */
expect fun isPdfTabClosed(): Boolean

/** Returns true when the user is likely viewing the PDF tab (it is open and focused). */
expect fun isPdfTabFocused(): Boolean
