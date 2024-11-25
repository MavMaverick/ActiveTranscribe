// DisplayManager.kt
package com.example.opentranscribe.display

import com.activelook.activelooksdk.Glasses

object DisplayManager {
    var asrTextStreamDisplay: ASRTextStreamDisplay? = null
    var connectedGlasses: Glasses? = null // New global variable for the glasses object
}
