package com.example.opentranscribe

import android.Manifest
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.activelook.activelooksdk.Sdk
import com.activelook.activelooksdk.types.Rotation
import com.example.opentranscribe.ui.theme.MainScreen
import com.example.opentranscribe.ui.theme.OpenTranscribeTheme

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

        // Set up Compose UI with OpenTranscribeTheme and MainScreen
        setContent {
            OpenTranscribeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun initializeActiveLookSDK() {
        // Run SDK initialization and start scanning for glasses
        Sdk.init(
            this,
            { gu -> Log.d("GLASSES_UPDATE", "onUpdateStart               : $gu") },
            { gu_f ->
                Log.d("GLASSES_UPDATE", "onUpdateAvailableCallback   : ${gu_f.first}")
                gu_f.second.run()
            },
            { gu -> Log.d("GLASSES_UPDATE", "onUpdateProgress            : $gu") },
            { gu -> Log.d("GLASSES_UPDATE", "onUpdateSuccess             : $gu") },
            { gu -> Log.d("GLASSES_UPDATE", "onUpdateError               : $gu") }
        )

        // Start scanning for ActiveLook glasses
        Sdk.getInstance().startScan { discoveredGlasses ->
            Log.e("DISCOVER", "Glasses connecting: ${discoveredGlasses.address}")

            // Attempt to connect to the glasses
            discoveredGlasses.connect(
                { glasses ->
                    Log.e("CONNECT", "Glasses connecting")
                    glasses.clear()
                    // Initialize ASRTextStreamDisplay and store it in DisplayManager
                    DisplayManager.asrTextStreamDisplay = ASRTextStreamDisplay(glasses)
                    Log.e("CONNECT", "Glasses connected")
                },
                { error ->
                    Log.e("ERROR", "Glasses could not be connected")
                },
                {
                    Log.e("DISCONNECT", "Glasses have been disconnected")
                    DisplayManager.asrTextStreamDisplay = null // Clear the display reference
                }
            )
        }
    }
}
