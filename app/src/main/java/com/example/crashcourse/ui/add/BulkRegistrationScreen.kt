package com.example.crashcourse.ui.add

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.viewmodel.BulkRegistrationViewModel
import com.example.crashcourse.viewmodel.BulkRegistrationViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkRegistrationScreen() {

    // --------------------------------------------------
    // Context & ViewModel (WITH FACTORY)
    // --------------------------------------------------
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val bulkViewModel: BulkRegistrationViewModel = viewModel(
        factory = BulkRegistrationViewModelFactory(application)
    )

    val state by bulkViewModel.state.collectAsState()

    // --------------------------------------------------
    // UI State
    // --------------------------------------------------
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --------------------------------------------------
    // CSV Picker
    // --------------------------------------------------
    val csvLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            val resolver = context.contentResolver
            var displayName: String? = null

            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && idx != -1) {
                    displayName = cursor.getString(idx)
                }
            }

            val mime = resolver.getType(uri)?.lowercase() ?: ""
            val isCsv =
                mime.contains("csv") || displayName?.endsWith(".csv", true) == true

            if (isCsv) {
                fileUri = uri
                fileName = displayName ?: "selected.csv"
                errorMessage = null
                bulkViewModel.resetState()
                bulkViewModel.prepareProcessing(context, uri)
            } else {
                errorMessage = "Only CSV files are supported"
            }
        }

    // --------------------------------------------------
    // UI
    // --------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bulk Registration") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ==============================
            // SELECT CSV
            // ==============================
            Text(
                text = "Batch Registration",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { csvLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isProcessing
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select CSV File")
            }

            fileName?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Selected file: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            state.estimatedTime?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            errorMessage?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = Color.Red)
            }

            // ==============================
            // START PROCESS
            // ==============================
            if (fileUri != null && !state.isProcessing) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        bulkViewModel.processCsvFile(context, fileUri!!)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Batch Processing")
                }
            }

            // ==============================
            // PROGRESS
            // ==============================
            if (state.isProcessing) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = state.progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(state.status)
            }

            // ==============================
            // RESULTS
            // ==============================
            if (state.results.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Results",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .fillMaxWidth()
                ) {
                    items(state.results) { result ->
                        val color = when {
                            result.status == "Registered" -> Color(0xFF2E7D32)
                            result.status.startsWith("Duplicate") -> Color(0xFFFF8F00)
                            else -> Color(0xFFC62828)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        result.status == "Registered" ->
                                            Icons.Default.CheckCircle
                                        result.status.startsWith("Duplicate") ->
                                            Icons.Default.Warning
                                        else ->
                                            Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = color
                                )

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = result.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = result.status,
                                        color = color,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
