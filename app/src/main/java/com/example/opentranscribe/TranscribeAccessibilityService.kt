package com.example.opentranscribe

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TranscribeAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "TranscribeService"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            Log.d(TAG, "Received null event.")
            return
        }

        // Filter for Live Transcribe package only
        val packageName = event.packageName?.toString()
        if (packageName != "com.google.audio.hearing.visualization.accessibility.scribe") {
            Log.d(TAG, "Ignoring event from package: $packageName")
            return
        }

        Log.d(TAG, "onAccessibilityEvent triggered for Live Transcribe.")

        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.d(TAG, "Root node is null.")
            return
        }

        Log.d(TAG, "Root node accessed successfully.")

        // Search for the target node containing the transcription text
        val targetNode = findTextNode(rootNode)
        if (targetNode != null) {
            Log.d(TAG, "Extracted Text: ${targetNode.text}")
        } else {
            Log.d(TAG, "No text node found with non-empty content.")
        }
    }

    override fun onInterrupt() {
        // Handle any interruptions here
    }

    // Utility method to recursively find a TextView with non-empty text
    private fun findTextNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null

        // Check if the current node is a TextView with non-empty text
        if ("android.widget.TextView" == node.className && !node.text.isNullOrEmpty()) {
            return node
        }

        // Recursively search child nodes
        for (i in 0 until node.childCount) {
            val foundNode = findTextNode(node.getChild(i))
            if (foundNode != null) {
                return foundNode
            }
        }

        return null
    }

    // Utility method to recursively find node by resource ID
    private fun findNodeById(node: AccessibilityNodeInfo?, resourceId: String): AccessibilityNodeInfo? {
        if (node == null) return null

        if (resourceId == node.viewIdResourceName) {
            return node
        }

        for (i in 0 until node.childCount) {
            val foundNode = findNodeById(node.getChild(i), resourceId)
            if (foundNode != null) {
                return foundNode
            }
        }

        return null
    }
}
