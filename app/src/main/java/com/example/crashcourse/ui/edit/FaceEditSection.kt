package com.example.crashcourse.ui.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.crashcourse.viewmodel.EditUserUiState

@Composable
fun FaceEditSection(
    uiState: EditUserUiState,
    onCaptureEmbedding: () -> Unit,
    onCapturePhoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Face & Photo",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCaptureEmbedding
            ) {
                Text(
                    text = if (uiState.embedding == null)
                        "Scan Face for Embedding"
                    else
                        "Embedding Captured ✓"
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCapturePhoto
            ) {
                Text(
                    text = if (uiState.capturedBitmap == null)
                        "Capture Photo"
                    else
                        "Photo Captured ✓"
                )
            }

            val displayBitmap =
                uiState.capturedBitmap ?: uiState.currentPhotoBitmap

            if (displayBitmap != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = displayBitmap.asImageBitmap(),
                        contentDescription = "User photo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (uiState.capturedBitmap != null)
                            "New photo selected"
                        else
                            "Current photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
