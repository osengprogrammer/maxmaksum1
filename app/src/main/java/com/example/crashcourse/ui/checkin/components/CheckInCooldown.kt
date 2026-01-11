package com.example.crashcourse.ui.checkin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box

@Composable
fun CheckInCooldown(
    alreadyCheckedIn: Boolean,
    remainingSeconds: Long
) {
    if (!alreadyCheckedIn || remainingSeconds <= 0) return

    Box(
        modifier = Modifier.padding(top = 140.dp)
    ) {
        Text(
            text = "Please wait $remainingSeconds seconds",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.Yellow,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
