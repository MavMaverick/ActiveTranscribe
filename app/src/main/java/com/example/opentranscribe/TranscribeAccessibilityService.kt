package com.example.opentranscribe

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class TranscribeAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "TranscribeService"
    }

    private var lastExtractedText: String? = null
    private lateinit var webSocket: WebSocket

    override fun onCreate() {
        super.onCreate()
        // Setup WebSocket
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("ws://192.168.0.108:8080")
            .build()

        webSocket = client.newWebSocket(request, WebSocketListenerImpl())
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
            val extractedText = targetNode.text?.toString()

            if (extractedText != lastExtractedText) {
                lastExtractedText = extractedText

                Log.d(TAG, "Extracted Text: $extractedText")

                // Send text via WebSocket
                webSocket.send(extractedText ?: "")
            }
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

    // WebSocket Listener to manage connection events
    private inner class WebSocketListenerImpl : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            Log.d(TAG, "WebSocket connection opened.")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Receiving message: $text")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Receiving bytes: $bytes")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket connection closed: $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
        }
    }
}
