package com.example.opentranscribe.display

import com.activelook.activelooksdk.Glasses
import com.example.opentranscribe.util.wrapper

class ASRTextStreamDisplay(private val glasses: Glasses) {

    private val finalizedLines = mutableListOf<String>() // Store finalized lines
    private var activeBlock: String = "" // Current block being generated
    private val maxLinesOnScreen: Int = 6 // Maximum number of visual lines
    private val maxFinalizedLines: Int = 7 // Maximum stored finalized lines
    private val lineSpacing: Int = 19 // Spacing for each line

    /**
     * Process incoming ASR message and update the buffer.
     * @param status "FIN" or "GEN" indicating if the line is finalized or still generating.
     * @param text The ASR-transcribed text to process.
     */
    fun processASRMessage(status: String, text: String) {
        if (status == "FIN") {
            // Wrap and add active block to finalized lines, then reset active block
            val wrappedActiveBlock = wrapper(activeBlock, maxCharLimit = 26)
            finalizedLines.addAll(wrappedActiveBlock)
            // Until a monospace font is added, use '_' not " ", " " uses too little space to
            // clear text by overlapping.
            finalizedLines.add("__________________________") // Add a blank line as a separator
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
            displayLines.addAll(wrapper(activeBlock, maxCharLimit = 26))
        }

        // Start rendering from the bottom of the screen
        var yOffset = 160
        var isFlagSet = false

        // Apply scrolling logic: only show the last `maxLinesOnScreen` lines
        val trimmedDisplayLines = if (displayLines.size > maxLinesOnScreen) {
            isFlagSet = true
            displayLines.takeLast(maxLinesOnScreen)
        } else {
            displayLines
        }

        if (isFlagSet) { // set because buffer overflow, need shift and print all lines
            // Render the lines in the correct sequence for downward printing
            for (line in trimmedDisplayLines) { // Remove reversed() here

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
        } else { // not set because no overflow, don't need to print all lines every change
            for ((index, line) in trimmedDisplayLines.withIndex()) {

                // Check if this is the last item in the list
                if (index == trimmedDisplayLines.lastIndex) {
                    // Call glasses.txt only for the last item
                    glasses.txt(
                        280.toShort(), // Adjusted X-coordinate to align text
                        yOffset.toShort(), // Y-coordinate for text (starts from the bottom and moves upward)
                        com.activelook.activelooksdk.types.Rotation.TOP_LR,
                        0x01.toByte(), // Font size (as Byte
                        0xFF.toByte(), // Yellow color (as Byte)
                        line
                    )
                }

                yOffset -= lineSpacing + 5 // Decrement Y-coordinate for the next line
            }
        }
    }
}

