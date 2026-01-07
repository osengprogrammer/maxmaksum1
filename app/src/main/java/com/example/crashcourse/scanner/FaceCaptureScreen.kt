package com.example.crashcourse.scanner

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.crashcourse.ml.FaceAnalyzer
import com.example.crashcourse.ui.FaceOverlay
import com.example.crashcourse.utils.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun FaceCaptureScreen(
    mode: CaptureMode,
    controller: FaceAnalyzerController,
    onClose: () -> Unit,
    onEmbeddingCaptured: (FloatArray) -> Unit = {},
    onPhotoCaptured: (Bitmap) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var useFrontCamera by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var captureSuccess by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val flashAlpha = remember { Animatable(0f) }
    val checkScale = remember { Animatable(0.5f) }

    val captureExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    DisposableEffect(Unit) {
        onDispose { captureExecutor.shutdown() }
    }

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    val analyzer = remember(controller) {
    FaceAnalyzer(controller)
}


    LaunchedEffect(useFrontCamera) {
        controller.resetAll()
    }

    // Feedback animation
    LaunchedEffect(showFeedback) {
        if (!showFeedback) return@LaunchedEffect

        flashAlpha.animateTo(1f, tween(80))
        flashAlpha.animateTo(0f, tween(250))

        checkScale.animateTo(
            1.2f,
            spring(dampingRatio = Spring.DampingRatioLowBouncy)
        )
        checkScale.animateTo(1f, tween(250))

        delay(900)
        showFeedback = false
        capturedBitmap = null
    }

    Box(modifier = Modifier.fillMaxSize()) {

        CameraPermissionGate {
            FaceCameraHost(
                useFrontCamera = useFrontCamera,
                analyzer = analyzer,
                imageCapture = imageCapture,
                modifier = Modifier.fillMaxSize()
            )
        }

        FaceOverlay(
            faceBounds = controller.faceBounds,
            imageSize = controller.imageSize,
            imageRotation = controller.imageRotation,
            isFrontCamera = useFrontCamera,
            modifier = Modifier.fillMaxSize(),
            paddingFactor = 0.1f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = flashAlpha.value))
        )

        Text(
            text = when {
                !controller.hasFace ->
                    "🔍 Align your face inside the frame"

                controller.hasMultipleFaces ->
                    "⚠️ One person only"

                else -> when (mode) {
                    CaptureMode.EMBEDDING -> "✅ Face ready – capture embedding"
                    CaptureMode.PHOTO -> "✅ Face ready – take photo"
                }
            },
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            Button(
                onClick = onClose,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cancel")
            }

            Button(
                enabled = !isProcessing && controller.embedding != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                onClick = {
                    coroutineScope.launch {
                        isProcessing = true
                        showFeedback = true
                        captureSuccess = false

                        when (mode) {
                            CaptureMode.EMBEDDING -> {
                                delay(700)
                                controller.embedding?.let {
                                    onEmbeddingCaptured(it)
                                    captureSuccess = true
                                    delay(600)
                                    onClose()
                                }
                            }

                            CaptureMode.PHOTO -> {
                                imageCapture.takePicture(
                                    captureExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {

                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            coroutineScope.launch {
                                                try {
                                                    val bitmap = image.toBitmap()
                                                    capturedBitmap = bitmap
                                                    onPhotoCaptured(bitmap)
                                                    captureSuccess = true
                                                    delay(500)
                                                    onClose()
                                                } finally {
                                                    image.close()
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isProcessing = false
                                            showFeedback = false
                                        }
                                    }
                                )
                            }
                        }

                        isProcessing = false
                    }
                }
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        when (mode) {
                            CaptureMode.EMBEDDING -> "Capture"
                            CaptureMode.PHOTO -> "Take Photo"
                        }
                    )
                }
            }
        }

        if (showFeedback && captureSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    capturedBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                        Spacer(Modifier.height(16.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(checkScale.value)
                            .background(Color.Green, CircleShape)
                            .padding(12.dp)
                    )
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        IconButton(
            onClick = { useFrontCamera = !useFrontCamera },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = "Switch", tint = Color.White)
        }
    }
}
