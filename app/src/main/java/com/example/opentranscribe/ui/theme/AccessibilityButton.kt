package com.example.opentranscribe.ui.theme

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height

@Composable
fun AccessibilityButton() {
    val context = LocalContext.current

    Button(
        onClick = {
            // Intent to open the Accessibility Settings
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        },
        modifier = Modifier
            .width(200.dp) // Set custom width
            .height(60.dp) // Set custom height
    ) {
        Text("Turn on")
    }
}
