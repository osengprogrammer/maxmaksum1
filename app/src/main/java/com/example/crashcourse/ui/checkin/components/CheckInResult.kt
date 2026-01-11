package com.example.crashcourse.ui.checkin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box

@Composable
fun CheckInResult(
    name: String,
    alreadyCheckedIn: Boolean,
    hour: Int
) {
    Box(
        modifier = Modifier
            .padding(top = 80.dp)
    ) {
        Text(
            text = if (alreadyCheckedIn) {
                "$name Already Checkin"
            } else {
                "$name Checkin at $hour:00"
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF008080),
                fontWeight = FontWeight.Bold
            )
        )
    }
}
