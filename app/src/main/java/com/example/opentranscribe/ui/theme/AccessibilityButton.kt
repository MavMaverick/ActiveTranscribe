package com.example.opentranscribe.ui.theme

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment


@Composable
fun AccessibilityButton() {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Button to open Accessibility Settings
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        ) {
            Text("Turn on")
        }

        Spacer(modifier = Modifier.size(16.dp)) // Spacer between buttons

        // Button to clear cache
        Button(
            onClick = {
                clearAppCache(context)
            },
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
        ) {
            Text("Clear Cache")
        }
    }
}
