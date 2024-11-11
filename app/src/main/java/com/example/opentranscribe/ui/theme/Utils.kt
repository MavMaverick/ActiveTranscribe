package com.example.opentranscribe.ui.theme

import android.content.Context
import android.util.Log

fun clearAppCache(context: Context) {
    try {
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
        Log.d("CacheClear", "Cache successfully cleared.")  // Success log
    } catch (e: Exception) {
        Log.e("CacheClear", "Failed to clear cache: ${e.message}")  // Error log
        e.printStackTrace()
    }
}
