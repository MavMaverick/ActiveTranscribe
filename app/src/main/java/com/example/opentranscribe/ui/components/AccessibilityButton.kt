package com.example.opentranscribe.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.activelook.activelooksdk.types.FontData
import com.example.opentranscribe.R


import com.example.opentranscribe.display.DisplayManager


@Composable
fun AccessibilityButton() {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // Button to open Accessibility Settings
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
        ) {
            Text("Turn on")
        }

        Spacer(modifier = Modifier.size(16.dp)) // Spacer between buttons

        // Button to open Accessibility Settings
        Button(
            onClick = {
                val glasses = DisplayManager.connectedGlasses
                if (glasses != null) {
                    val inputStream = context.resources.openRawResource(R.raw.font_data)
                    val bytes = inputStream.readBytes()


                    val fontData = FontData(bytes);

                    glasses.cfgDelete("ATrans")
                    glasses.cfgWrite("ATrans", 1, 42)
                    glasses.cfgSet("ATrans")
                    glasses.fontDelete(0x06.toByte())
                    glasses.fontSave(0x06.toByte(), fontData)

                    glasses.txt(
                        260.toShort(), // Adjusted X-coordinate to align text
                        180.toShort(), // Y-coordinate for text (starts from the bottom and moves upward)
                        com.activelook.activelooksdk.types.Rotation.TOP_LR,
                        0x06.toByte(), // Font size (as Byte
                        0xFF.toByte(), // Yellow color (as Byte)
                        "Hello worrld. The brown foix jumps"
                    )
                    glasses.txt(
                        180.toShort(), // Adjusted X-coordinate to align text
                        240.toShort(), // Y-coordinate for text (starts from the bottom and moves upward)
                        com.activelook.activelooksdk.types.Rotation.TOP_LR,
                        0x01.toByte(), // Font size (as Byte
                        0xFF.toByte(), // Yellow color (as Byte)
                        "Hello world"
                    )

                    // List available fonts to verify
                    glasses.cfgList { cfgList ->
                        cfgList.forEach { cfg ->
                            println("Configuration Name: ${cfg.name}")
                                }
                            }
                } else {
                    println("No connected glasses found!")
                }
            },
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
        ) {
            Text("Upload Font")
        }

        Spacer(modifier = Modifier.size(16.dp)) // Spacer between buttons

        // Button to clear display
        Button(
            onClick = {
//                clearAppCache(context)
                DisplayManager.connectedGlasses?.clear() // Use the new global variable
            },
            modifier = Modifier
                .width(120.dp)
                .height(60.dp)
        ) {
            Text("Clear Display")
        }
    }
}
