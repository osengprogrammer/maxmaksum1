package com.example.crashcourse.ui.checkin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.snapshotFlow
import com.example.crashcourse.audio.TTSManager
import com.example.crashcourse.scanner.FaceAnalyzerController
import com.example.crashcourse.scanner.FaceScanner
import com.example.crashcourse.ui.checkin.components.*
import com.example.crashcourse.ui.checkin.recognition.CheckInRecognitionViewModel
import java.util.Calendar

@Composable
fun CheckInScreen(
    useBackCamera: Boolean
) {
    val context = LocalContext.current
    var currentCameraIsBack by remember { mutableStateOf(useBackCamera) }

    val recognitionViewModel: CheckInRecognitionViewModel = viewModel()
    val state by recognitionViewModel.state.collectAsStateWithLifecycle()

    val faceController = remember { FaceAnalyzerController() }

    val ttsManager = remember { TTSManager(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Forward embeddings → ViewModel
    LaunchedEffect(faceController) {
        snapshotFlow { faceController.embedding }
            .collect { embedding ->
                embedding?.let { recognitionViewModel.onEmbeddingReceived(it) }
            }
    }

    // Speak only on NEW successful check-in
    LaunchedEffect(state.matchName, state.alreadyCheckedIn) {
        if (state.matchName != null && !state.alreadyCheckedIn) {
            ttsManager.speak("Thanks")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (state.loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }

        FaceScanner(
            controller = faceController,
            useBackCamera = currentCameraIsBack,
            modifier = Modifier.fillMaxSize()
        )

        CheckInHeader(
            currentCameraIsBack = currentCameraIsBack,
            onSwitchCamera = { currentCameraIsBack = !currentCameraIsBack }
        )

        state.matchName?.let { name ->
            CheckInResult(
                name = name,
                alreadyCheckedIn = state.alreadyCheckedIn,
                hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            )
        }

        CheckInCooldown(
            alreadyCheckedIn = state.alreadyCheckedIn,
            remainingSeconds = state.remainingCooldownSeconds
        )

        CheckInNotRegistered(
            notRegistered = state.notRegistered
        )
    }
}
