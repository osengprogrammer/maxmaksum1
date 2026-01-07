package com.example.crashcourse.ui.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.scanner.FaceAnalyzerController
import com.example.crashcourse.scanner.FaceCaptureScreen
import com.example.crashcourse.viewmodel.FaceViewModel
import kotlinx.coroutines.launch

@Composable
fun AddUserScreen(
    onNavigateBack: () -> Unit = {},
    onUserAdded: () -> Unit = {}
) {
    // ---------- STATE ----------
    var uiState by remember { mutableStateOf(AddUserUiState()) }

    // ---------- VIEWMODELS ----------
    val addUserViewModel: AddUserViewModel = viewModel()
    val faceViewModel: FaceViewModel = viewModel()

    // ---------- HELPERS ----------
    val coordinator = remember { FaceCaptureCoordinator() }
    val faceController = remember { FaceAnalyzerController() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ---------- SIDE EFFECT ----------
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar(
                message = "Registered successfully!",
                duration = SnackbarDuration.Short
            )
        }
    }

    // ---------- UI ----------
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding),
                verticalArrangement = Arrangement.Top
            ) {

                // ---------- HEADER ----------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add New User",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ---------- FORM ----------
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = {
                        uiState = uiState.copy(name = it, isSaved = false)
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.studentId,
                    onValueChange = {
                        uiState = uiState.copy(studentId = it, isSaved = false)
                    },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // ---------- FACE & PHOTO ----------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Text(
                            text = "Face & Photo",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // --- EMBEDDING ---
                        Button(
                            onClick = {
                                uiState = coordinator.openEmbedding(uiState)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (uiState.embedding == null)
                                    "Scan Face for Embedding"
                                else
                                    "Embedding Captured ✅"
                            )
                        }

                        if (uiState.embedding != null) {
                            Text(
                                text = "✅ Face embedding captured",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // --- PHOTO ---
                        Button(
                            onClick = {
                                uiState = coordinator.openPhoto(uiState)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (uiState.capturedBitmap == null)
                                    "Capture Photo"
                                else
                                    "Photo Captured ✅"
                            )
                        }

                        uiState.capturedBitmap?.let { bitmap ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "User Photo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("✅ Photo captured")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ---------- REGISTER BUTTON ----------
               Button(
    onClick = {
        if (!uiState.canSubmit) return@Button

        uiState = uiState.copy(isSubmitting = true)

        addUserViewModel.registerUser(
            context = context,
            state = uiState,
            faceViewModel = faceViewModel,
            onSuccess = {
                uiState = AddUserUiState(isSaved = true)
                onUserAdded()
            },
            onDuplicate = { existingName ->
                uiState = uiState.copy(isSubmitting = false)
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Duplicate detected: $existingName"
                    )
                }
            },
            onError = { message ->
                uiState = uiState.copy(isSubmitting = false)
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        )
    },
    enabled = uiState.canSubmit && !uiState.isSubmitting, // ✅ correct
    modifier = Modifier.fillMaxWidth()
)
 {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Register User")
                    }
                }
            }

            // ---------- FACE CAPTURE OVERLAY ----------
            if (uiState.showFaceCapture) {
                FaceCaptureScreen(
                    mode = uiState.captureMode,
                    controller = faceController,
                    onClose = {
                        uiState = uiState.copy(showFaceCapture = false)
                    },
                    onEmbeddingCaptured = { embedding ->
                        uiState = coordinator.onEmbeddingCaptured(uiState, embedding)
                    },
                    onPhotoCaptured = { bitmap ->
                        uiState = coordinator.onPhotoCaptured(uiState, bitmap)
                    }
                )
            }
        }
    }
}
