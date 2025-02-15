package com.example.opentranscribe.core

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.activelook.activelooksdk.Sdk
import com.example.opentranscribe.display.ASRTextStreamDisplay
import com.example.opentranscribe.display.DisplayManager
import com.example.opentranscribe.ui.theme.OpenTranscribeTheme
import androidx.compose.ui.Alignment
import com.example.opentranscribe.ui.components.AccessibilityButton

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions for BLE and Internet access
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ),
            0
        )

        // Initialize the SDK with update handling
        initializeActiveLookSDK()

        // Set up Compose UI with OpenTranscribeTheme and directly include AccessibilityButton
        setContent {
            OpenTranscribeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        // AccessibilityButton.kt
                        AccessibilityButton()
                    }
                }
            }
        }
    }


    private fun initializeActiveLookSDK() {
        // Run SDK initialization and start scanning for glasses
        Sdk.init(
            this,
            { glassesUpdateStart ->
                Log.d("GLASSES_UPDATE","onUpdateStart: $glassesUpdateStart")
            },
            { updateInfo ->
                Log.d("GLASSES_UPDATE", "onUpdateAvailableCallback: ${updateInfo.first}")
                updateInfo.second.run()
            },
            { updateProgress ->
                Log.d("GLASSES_UPDATE","onUpdateProgress: $updateProgress")
            },
            { updateSuccess ->
                Log.d("GLASSES_UPDATE","onUpdateSuccess: $updateSuccess")
            },
            { updateError -> Log.d("GLASSES_UPDATE", "onUpdateError: $updateError") }
        )

        // Start scanning for ActiveLook glasses
        Sdk.getInstance().startScan { discoveredDevice ->
            Log.e("DISCOVER", "Glasses connecting: ${discoveredDevice.address}")

            // Attempt to connect to the glasses
            discoveredDevice.connect(
                { connectedGlasses ->
                    Log.e("CONNECT", "Glasses connecting")
                    // Clear display for clean slate
                    connectedGlasses.clear()
                    // Initialize ASRTextStreamDisplay and store it in DisplayManager
                    DisplayManager.asrTextStreamDisplay = ASRTextStreamDisplay(connectedGlasses)
                    DisplayManager.connectedGlasses = connectedGlasses // Update the new global variable
                    Log.e("CONNECT", "Glasses connected")
                },
                { connectionError ->
                    Log.e("ERROR", "Glasses could not be connected: $connectionError")
                },
                {
                    Log.e("DISCONNECT", "Glasses have been disconnected")
                    DisplayManager.asrTextStreamDisplay = null // Clear the display reference
                    DisplayManager.connectedGlasses = null // Clear the global variable
                }
            )
        }
    }
}