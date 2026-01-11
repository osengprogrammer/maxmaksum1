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
fun CheckInNotRegistered(
    notRegistered: Boolean
) {
    if (!notRegistered) return

    Box {
        Text(
            text = "Not Registered",
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.error
        )
    }
}
