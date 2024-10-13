package com.example.opentranscribe

// Importing necessary Android and OkHttp libraries
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
        const val TAG = "TranscribeService" // Logging tag for the service
    }

    private var lastExtractedText: String? = null // To store the last extracted text for comparison
    private lateinit var webSocket: WebSocket // WebSocket to handle communication with server

    // Called when the service is created, used to set up WebSocket
    override fun onCreate() {
        super.onCreate()

        // Setting up OkHttp WebSocket client with 3 seconds read timeout
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        // Building WebSocket request with the specified server URL
        val request = Request.Builder()
            .url("ws://192.168.0.108:8080") // WebSocket server URL
            .build()

        // Initializing WebSocket connection
        webSocket = client.newWebSocket(request, WebSocketListenerImpl())
    }

    // Called when an accessibility event occurs
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Check if the event is null
        if (event == null) {
            Log.d(TAG, "Received null event.")
            return
        }

        // Filtering for events only from the Live Transcribe app package
        val packageName = event.packageName?.toString()
        if (packageName != "com.google.audio.hearing.visualization.accessibility.scribe") {
            Log.d(TAG, "Ignoring event from package: $packageName")
            return
        }

        Log.d(TAG, "onAccessibilityEvent triggered for Live Transcribe.")

        // Accessing the root node of the active window (UI hierarchy)
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.d(TAG, "Root node is null.")
            return
        }

        Log.d(TAG, "Root node accessed successfully.")

        // Searching for the target node that contains the transcription text
        val targetNode = findTextNode(rootNode)
        if (targetNode != null) {
            // Extracting text from the found node
            val extractedText = targetNode.text?.toString()

            // Sending the text via WebSocket if it's different from the last extracted text
            if (extractedText != lastExtractedText) {
                lastExtractedText = extractedText // Updating last extracted text

                Log.d(TAG, "Extracted Text: $extractedText")

                // Sending the extracted text to WebSocket
                webSocket.send(extractedText ?: "")
            }
        } else {
            Log.d(TAG, "No text node found with non-empty content.")
        }
    }

    // Called when the accessibility service is interrupted
    override fun onInterrupt() {
        // Handle any interruptions here, such as when another accessibility service takes priority
    }

    // Recursive utility method to search for a TextView with non-empty text
    private fun findTextNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null // Return if node is null

        // Check if the current node is a TextView and contains non-empty text
        if ("android.widget.TextView" == node.className && !node.text.isNullOrEmpty()) {
            return node // Return the node if the condition matches
        }

        // Recursively search child nodes
        for (i in 0 until node.childCount) {
            val foundNode = findTextNode(node.getChild(i))
            if (foundNode != null) {
                return foundNode // Return the found node if any
            }
        }

        return null // Return null if no matching node is found
    }

    // WebSocketListener to handle WebSocket events
    private inner class WebSocketListenerImpl : WebSocketListener() {
        // Called when the WebSocket connection is opened
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            Log.d(TAG, "WebSocket connection opened.")
        }

        // Called when a text message is received from the WebSocket server
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Receiving message: $text")
        }

        // Called when a binary message is received from the WebSocket server
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Receiving bytes: $bytes")
        }

        // Called when the WebSocket connection is closed
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket connection closed: $reason")
        }

        // Called when the WebSocket connection fails
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}")
        }
    }
}
