package com.example.opentranscribe

import android.util.Log
import com.activelook.activelooksdk.Glasses
import com.example.opentranscribe.TranscribeAccessibilityService.Companion.TAG
import com.example.opentranscribe.ui.theme.wrapper

class ASRTextStreamDisplay(private val glasses: Glasses) {

    private val finalizedLines = mutableListOf<String>() // Store finalized lines
    private var activeBlock: String = "" // Current block being generated
    private val maxLinesOnScreen: Int = 4 // Maximum number of visual lines
    private val maxFinalizedLines: Int = 5 // Maximum stored finalized lines
    private val lineSpacing: Int = 19 // Spacing for each line
    private var yOffsetStart: Int = 25 // Starting y-coordinate for rendering

    /**
     * Process incoming ASR message and update the buffer.
     * @param status "FIN" or "GEN" indicating if the line is finalized or still generating.
     * @param text The ASR-transcribed text to process.
     */
    fun processASRMessage(status: String, text: String) {
        if (status == "FIN") {
            // Wrap and add active block to finalized lines, then reset active block
            val wrappedActiveBlock = wrapper(activeBlock, maxCharLimit = 29)
            finalizedLines.addAll(wrappedActiveBlock)
            finalizedLines.add("") // Add a blank line as a separator
            activeBlock = text // Start a new active block

            // Trim finalized lines to max limit
            if (finalizedLines.size > maxFinalizedLines) {
                val excess = finalizedLines.size - maxFinalizedLines
                finalizedLines.subList(0, excess).clear() // Remove the oldest lines
            }
        } else {
            // Update the active block text with new generating content
            activeBlock = text
        }

        // Update the display
        updateDisplay()
    }

    /**
     * Render the lines on the glasses display using ActiveLook commands.
     */
    private fun updateDisplay() {
        // Combine finalized lines with the current wrapped active block
        val displayLines = finalizedLines.toMutableList()
        if (activeBlock.isNotEmpty()) {
            // Wrap and add active block lines in the correct order
            displayLines.addAll(wrapper(activeBlock, maxCharLimit = 29))
        }

        // Apply scrolling logic: only show the last `maxLinesOnScreen` lines
        val trimmedDisplayLines = if (displayLines.size > maxLinesOnScreen) {
            displayLines.takeLast(maxLinesOnScreen)
        } else {
            displayLines
        }

        // Clear the display
        glasses.clear()

        // Start rendering from the bottom of the screen
        var yOffset = 160

        // Render the lines in the correct sequence for downward printing
        for (line in trimmedDisplayLines) { // Remove reversed() here
            Log.d(TAG, "Printing Line")
            glasses.txt(
                280.toShort(), // Adjusted X-coordinate to align text
                yOffset.toShort(), // Y-coordinate for text (starts from the bottom and moves upward)
                com.activelook.activelooksdk.types.Rotation.TOP_LR,
                0x01.toByte(), // Font size (as Byte)
                0xFF.toByte(), // Yellow color (as Byte)
                line
            )
            yOffset -= lineSpacing + 5 // Decrement Y-coordinate for the next line
        }
    }

}

