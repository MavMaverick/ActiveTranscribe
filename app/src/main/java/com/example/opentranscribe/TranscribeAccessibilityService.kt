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

    private var lastExtractedText: String? = null // Stores the last extracted text to prevent duplicate sending
    private var newLines: List<String>? = null // Stores the last 3 lines of current text
    private var oldLines: List<String>? = null // Stores the previous 3 lines for comparison

    private lateinit var webSocket: WebSocket // WebSocket instance for server communication

    // Initializes the WebSocket connection on service creation
    override fun onCreate() {
        super.onCreate()

        // Setting up OkHttp WebSocket client with 3 seconds read timeout
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        // Build and initialize WebSocket with the target server URL
        val request = Request.Builder()
            .url("ws://192.168.0.52:8080") // Replace with actual WebSocket server URL
            // 192.168.0.52, replace with your laptops IP
            // replace :8080 with the port you want to use or leave it alone
            // Don't forget to also change WebSocket URL in res/xml/network_security_config.xml
            // Don't forget to change the server.js URL as well (the file isn't in this repo)
            .build()
        webSocket = client.newWebSocket(request, WebSocketListenerImpl())
    }

    // Handles accessibility events triggered by other applications
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Ensure event is non-null
        if (event == null) {
            Log.d(TAG, "Received null event.")
            return
        }

        // Process only events from the Live Transcribe app package
        val packageName = event.packageName?.toString()
        if (packageName != "com.google.audio.hearing.visualization.accessibility.scribe") {
            Log.d(TAG, "Ignoring event from package: $packageName")
            return
        }

        // Retrieve the root node of the active window's UI hierarchy
        val rootNode = rootInActiveWindow
        if (rootNode == null) {
            Log.d(TAG, "Root node is null.")
            return
        }

        // Searching for the target node that contains the transcription text
        val targetNode = findTextNode(rootNode)
        if (targetNode != null) {
            // Extracting text from the found node
            val extractedText = targetNode.text?.toString()

            // Sending the text via WebSocket if it's different from the last extracted text
            // Necessary because there's spam output of the same thing and we only want
            // new output, this 'filters' that
            if (extractedText != lastExtractedText) {
//                Log.d(TAG, "Extracted Text: $extractedText")

                lastExtractedText = extractedText // Updating last extracted text
                // This should take the string, which will contain \n\n between lines, and make
                // them into a string list only containing the useful strings, no empty ones.
                val extractedLines = extractedText?.split("\n\n")

                // Only keep the last 3 lines if they exist, this is to prevent processing
                // massive transcript histories captured by the ASR.
                val last3Lines = extractedLines?.takeLast(3)
                // This removes any whitespace from the string list items.
                val last3LinesTrimmed = last3Lines?.map { it.trim() }
                // Get the last non-empty element in the string list
                // update newLines with  current ASR output lines
                newLines = last3LinesTrimmed

                // if oldLines exist, compare against newLines. The reason we check for oldLines is
                // because we to to be able to compare to know if the ASR is still generating, or
                // if it has finished.
                if (oldLines != null && newLines != null){
                    // Smart casting here instead of forcing !!
                    val smartOldLines = oldLines
                    val smartNewLines = newLines
                    // if two or more smartOldLines exist
                    if (smartOldLines != null && smartOldLines.size >= 2 && smartNewLines != null) {
//                        Log.d(TAG, "IF CHECK PASSED")
                        val oldElement = smartOldLines[smartOldLines.size - 1]
                        val newElement = smartNewLines[smartNewLines.size - 2]
                        val curElement = smartNewLines[smartNewLines.size - 1]
                        /*
                                    oldLines (last three lines as of previous extraction)
                        [-2] Sure. Sounds good. What time do you have in mind?
                        [-1] I was thinking around 6 p.m the weather should be cool

                                    newLines (current three lines as of the current extraction)
                        [-3] Sure. Sounds good. What time do you have in mind?
                        [-2] I was thinking around 6 p.m the weather should be cool.
                        [-1] Awesome

                        We compare if oldElement[-1] != newElement[-2], and if they match, we know
                        the ASR is generating and not finalized. By checking against the previous
                        line we always have a way of knowing when a line is done or not; generating
                        or finalized. Because of this, at the very start when we have no oldLines,
                        I have defaulted to using flag GEN (even if it should be FIN) for when no
                        oldLines, and when oldLines < 2; need to fix but not worried about it.
                        Speak twice, with pauses, so two 'final' lines (with line-breaks) appear
                        on Google's Live Transcribe App screen which will then populate oldLines
                        which will fix the problem for the rest of the session.

                        NOTE: My code doesn't use all three lines but I trim down to 3 just in case.
                         */

                        // so we know it is still generating.
                        if (oldElement != newElement) {
//                            Log.d(TAG, "Generating")
                            Log.d(TAG, "GENERATING: $curElement")
                            sendMessage(curElement, "GEN")
                            oldLines = newLines

                        // Because oldElement is different, this means the ASR has added a new line
                        // because it finished generating text, meaning we know it is finished.
                        } else {
                            oldLines = newLines
                            // Log the final line item
                            Log.d(TAG, "FINALIZATION: $curElement\n\n")
                            sendMessage(curElement, "FIN")
                        }
                    // This occurs when oldLines doesn't have enough lines (2) for us to compare to
                    // know if we are still generating a line, or finalized.
                    } else {
                        // new becomes old, update string list oldLines
                        oldLines = newLines
                        Log.d(TAG, "PREV < 2: ${newLines?.lastOrNull()}")
                        sendMessage(newLines?.lastOrNull(), "GEN")
                    }
                // This happens typically right at the start of extraction when there's no text.
                // Just send last line, or NULL, of newLines.
                } else{
                    oldLines = newLines
                    Log.d(TAG, "NO PREV: ${newLines?.lastOrNull()}")
                    sendMessage(newLines?.lastOrNull(), "GEN")
                }
            }
        } else {
            Log.d(TAG, "No text node found with non-empty content.")
        }
    }
    // This helps reduce redundant json packaging code by making it a function.
    private fun sendMessage(content: String?, flag: String) {
        content?.let {
            val jsonMessage = """
                {
                    "flag": "$flag",
                    "content": "$content"
                }
            """.trimIndent()
            webSocket.send(jsonMessage)
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
