package com.example.crashcourse.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.scanner.CaptureMode
import com.example.crashcourse.utils.showToast
import com.example.crashcourse.viewmodel.EditUserViewModel
import com.example.crashcourse.viewmodel.EditUserViewModelFactory
import com.example.crashcourse.viewmodel.FaceViewModel
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.Error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    studentId: String,
    onNavigateBack: () -> Unit,
    onUserUpdated: () -> Unit,
    faceViewModel: FaceViewModel = viewModel()
) {
    val context = LocalContext.current

    // ViewModel (logic holder)
    val editViewModel: EditUserViewModel = viewModel(
        factory = EditUserViewModelFactory(faceViewModel)
    )

    // Load user
    val allFaces by faceViewModel.faceList.collectAsStateWithLifecycle(emptyList())
    val user = allFaces.find { it.studentId == studentId }

    LaunchedEffect(user) {
        user?.let { editViewModel.loadUser(it) }
    }

    // UI State
    val uiState by editViewModel.uiState.collectAsStateWithLifecycle()

    // UI-only capture state
    var showFaceCapture by remember { mutableStateOf(false) }
    var captureMode by remember { mutableStateOf(CaptureMode.EMBEDDING) }

    // Error state
    if (user == null) {
        ErrorStateScreen(
            title = "User Not Found",
            message = "The user with ID '$studentId' could not be found.",
            onNavigateBack = onNavigateBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Edit User")
                        if (uiState.hasUnsavedChanges) {
                            Text(
                                text = "Unsaved changes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(
                        enabled = uiState.hasUnsavedChanges && !uiState.isProcessing,
                        onClick = {
                            editViewModel.save(
                                onSuccess = {
                                    context.showToast("User updated successfully!")
                                    onUserUpdated()
                                    onNavigateBack()
                                },
                                onError = { msg ->
                                    context.showToast(msg)
                                }
                            )
                        }
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Student ID (read-only)
            OutlinedTextField(
                value = user.studentId,
                onValueChange = {},
                label = { Text("Student ID") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false
            )

            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = editViewModel::onNameChanged,
                label = { Text("Name *") },
                isError = uiState.nameError != null,
                supportingText = {
                    uiState.nameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Face & Photo (UI-only)
            FaceEditSection(
                uiState = uiState,
                onCaptureEmbedding = {
                    captureMode = CaptureMode.EMBEDDING
                    showFaceCapture = true
                },
                onCapturePhoto = {
                    captureMode = CaptureMode.PHOTO
                    showFaceCapture = true
                }
            )
        }
    }

    // Side-effect coordinator
    FaceCaptureCoordinator(
        show = showFaceCapture,
        captureMode = captureMode,
        onDismiss = { showFaceCapture = false },
        onEmbeddingCaptured = editViewModel::onEmbeddingCaptured,
        onPhotoCaptured = editViewModel::onPhotoCaptured
    )
}

@Composable
fun ErrorStateScreen(
    title: String,
    message: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

