package com.example.opentranscribe.ui.theme

import android.content.Context

fun clearAppData(context: Context) {
    // Clear preferences, cache, etc.
    context.cacheDir.deleteRecursively()
    context.filesDir.deleteRecursively()
    // Add other clearing logic as needed
}
