package com.example.crashcourse.ui.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.audio.TTSManager
import com.example.crashcourse.scanner.FaceAnalyzerController
import com.example.crashcourse.scanner.FaceScanner
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@Composable
fun CheckInScreen(
    useBackCamera: Boolean
) {
    val context = LocalContext.current
    var currentCameraIsBack by remember { mutableStateOf(useBackCamera) }

    // ViewModel (must exist as provided above)
    val recognitionViewModel: CheckInRecognitionViewModel = viewModel()
    val state by recognitionViewModel.state.collectAsStateWithLifecycle()

    // Camera/analyzer
    val faceController = remember { FaceAnalyzerController() }

    // TTS manager (UI side-effect)
    val ttsManager = remember { TTSManager(context.applicationContext) }
    DisposableEffect(ttsManager) {
        onDispose { ttsManager.shutdown() }
    }

    // Observe embeddings from the analyzer and forward to ViewModel (uses snapshotFlow)
    LaunchedEffect(faceController) {
        snapshotFlow { faceController.embedding }
            .collect { embedding: FloatArray? ->
                embedding?.let { recognitionViewModel.onEmbeddingReceived(it) }
            }
    }

    // Side-effect: speak when a new successful check-in occurs
    LaunchedEffect(state.matchName, state.alreadyCheckedIn) {
        if (state.matchName != null && !state.alreadyCheckedIn) {
            ttsManager.speak("Thanks")
        }


    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Loading indicator
        if (state.loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            // Camera preview (Composable)
            FaceScanner(
                controller = faceController,
                useBackCamera = currentCameraIsBack,
                modifier = Modifier.fillMaxSize()
            )

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Azura",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (currentCameraIsBack) "Back" else "Front",
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    IconButton(
                        onClick = { currentCameraIsBack = !currentCameraIsBack },
                        modifier = Modifier.background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }
            }

            // Result display
            state.matchName?.let { name ->
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                Text(
                    text = if (state.alreadyCheckedIn) "$name Already Checkin" else "$name Checkin at $hour:00",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(0xFF008080),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

                        // ⏳ Cooldown countdown display
            if (state.alreadyCheckedIn && state.remainingCooldownSeconds > 0) {
                Text(
                    text = "Please wait ${state.remainingCooldownSeconds} seconds",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 140.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Yellow,
                    style = MaterialTheme.typography.bodyLarge
                )
            }


            if (state.notRegistered) {
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
    }
}
