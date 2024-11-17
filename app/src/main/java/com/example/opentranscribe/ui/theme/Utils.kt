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

fun splitStringAtSpace(text: String, maxCharLimit: Int): Pair<String, String> {
    // If the text length is within the limit, return it as-is
    if (text.length <= maxCharLimit) {
        return Pair(text, "")
    }

    // If the next character after maxCharLimit's index is a space
    if (text[maxCharLimit] == ' ') {
        return Pair(text.substring(0, maxCharLimit), text.substring(maxCharLimit + 1))
    }

    // Search backward for the nearest space within the limit
    var currIndex = maxCharLimit - 1
    while (currIndex > 0 && text[currIndex] != ' ') {
        currIndex--
    }

    // If no space is found, hyphenate the word at the limit
    return if (currIndex == 0) {
        Pair(text.substring(0, maxCharLimit - 1) + "-", text.substring(maxCharLimit - 1))
    } else {
        Pair(text.substring(0, currIndex), text.substring(currIndex + 1))
    }
}

fun wrapper(words: String, maxCharLimit: Int): List<String> {
    val lines = mutableListOf<String>()
    var remainingText = words

    while (remainingText.isNotEmpty()) {
        val (line, rest) = splitStringAtSpace(remainingText, maxCharLimit)
        // Add underscores to the end of the line if it's shorter than maxCharLimit
        val paddedLine = line.padEnd(maxCharLimit, '_')
        lines.add(paddedLine)
        remainingText = rest
    }

    return lines
}


//fun main() {
//    val text = "This Supercalifragilisticexpialidocious. is a sample string that needs to be split into multiple lines."
//    val wrapped = wrapper(text, 10)
//    println("Wrapped lines:")
//    wrapped.forEach { println(it) }
//}
