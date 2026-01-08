package com.example.crashcourse.ui.add

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.viewmodel.FaceViewModel
import com.example.crashcourse.utils.PhotoProcessingUtils
import com.example.crashcourse.viewmodel.BulkRegistrationViewModel
import com.example.crashcourse.utils.PhotoStorageUtils

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkRegistrationScreen(
    faceViewModel: FaceViewModel = viewModel(),
    bulkViewModel: BulkRegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
    val bulkState by bulkViewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // ---------- Single Registration (Manual) ----------
    var name by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var embedding by remember { mutableStateOf<FloatArray?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // ---------- Batch Registration ----------
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }

    // ---------- Photo Picker ----------
    val photoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val bmp = PhotoStorageUtils.loadBitmapFromUri(context, it)

                if (bmp != null) {
                    coroutineScope.launch {
                        val result =
                            PhotoProcessingUtils.processBitmapForFaceEmbedding(context, bmp)
                        if (result != null) {
                            val (faceBitmap, emb) = result
                            bitmap = faceBitmap
                            embedding = emb
                            feedback = null
                        } else {
                            bitmap = bmp
                            embedding = null
                            feedback = "No face detected. Try another photo."
                        }
                    }
                }
            }
        }

    // ---------- CSV Picker ----------
    val fileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val resolver = context.contentResolver
                var nameFromMeta: String? = null

                resolver.query(it, null, null, null, null)?.use { cursor ->
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && idx != -1) {
                        nameFromMeta = cursor.getString(idx)
                    }
                }

                val mime = resolver.getType(it)?.lowercase() ?: ""
                val isCsv =
                    mime.contains("csv") || nameFromMeta?.endsWith(".csv", true) == true

                if (isCsv) {
                    fileUri = it
                    fileName = nameFromMeta ?: "selected.csv"
                    bulkViewModel.resetState()
                    bulkViewModel.prepareProcessing(context, it)
                } else {
                    feedback = "Only CSV files are supported"
                }
            }
        }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bulk Registration") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // =========================================================
            // SINGLE REGISTRATION
            // =========================================================
            Text("Single Registration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { photoLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select Photo")
            }

            bitmap?.let {
                Spacer(Modifier.height(12.dp))
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            feedback?.let {
                Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (embedding == null || name.isBlank()) {
                        feedback = "Name and valid face are required"
                        return@Button
                    }

                    isProcessing = true

                    faceViewModel.registerFace(
                        name = name.trim(),
                        embedding = embedding!!,
                        onSuccess = {
                            feedback = "Registration successful"
                            name = ""
                            bitmap = null
                            embedding = null
                            isProcessing = false
                        },
                        onDuplicate = {
                            feedback = "Duplicate face detected"
                            isProcessing = false
                        }
                    )
                },
                enabled = !isProcessing && embedding != null && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Register User")
                }
            }

            // =========================================================
            // BATCH REGISTRATION
            // =========================================================
            Spacer(Modifier.height(32.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            Text("Batch Registration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { fileLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !bulkState.isProcessing
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Select CSV File")
            }

            fileName?.let {
                Spacer(Modifier.height(8.dp))
                Text("Selected file: $it", style = MaterialTheme.typography.bodySmall)
            }

            if (fileUri != null && !bulkState.isProcessing) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { bulkViewModel.processCsvFile(context, fileUri!!) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Batch Processing")
                }
            }

            if (bulkState.isProcessing) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = bulkState.progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(bulkState.status)
            }

            if (bulkState.results.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Results", style = MaterialTheme.typography.titleMedium)

                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .fillMaxWidth()
                ) {
                    items(bulkState.results) { result ->
                        val color =
                            if (result.status == "Registered") Color.Green
                            else if (result.status.startsWith("Duplicate")) Color(0xFFFFA500)
                            else Color.Red

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when {
                                        result.status == "Registered" -> Icons.Default.Check
                                        result.status.startsWith("Duplicate") -> Icons.Default.Warning
                                        else -> Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = color
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = result.name,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(result.status, color = color)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
