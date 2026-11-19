package com.example.hr_employeemanagementsystem.ui.screens.enroll

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.camera.core.ImageAnalysis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.ml.EmbeddingUtils
import com.example.hr_employeemanagementsystem.ml.FaceDetectorManager
import com.example.hr_employeemanagementsystem.ml.FaceNetModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun FaceEnrollScreen(
    employeeId: Long,
    employeeRepository: EmployeeRepository,
    onEnrollSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var isCapturing by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var message by remember { mutableStateOf("Keep your face inside frame. Hold still for few seconds until scan is complete.") }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val faceNet = remember { FaceNetModel(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(
                                    cameraExecutor,
                                    FaceDetectorManager(
                                        onFaceDetected = { boundingBox, bitmap ->
                                            scope.launch { message = "Face detected! Tap capture to save." }
                                            if (isCapturing) {
                                                isCapturing = false
                                                try {
                                                    val liveEmbedding = faceNet.getFaceEmbedding(bitmap, boundingBox)
                                                    val descriptorString = EmbeddingUtils.floatArrayToString(liveEmbedding)

                                                    val file = java.io.File(context.filesDir, "face_${employeeId}_${System.currentTimeMillis()}.jpg")
                                                    java.io.FileOutputStream(file).use { out ->
                                                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                                    }

                                                    scope.launch {
                                                        employeeRepository.saveFaceEnrollment(employeeId, descriptorString)
                                                        val emp = employeeRepository.getEmployeeById(employeeId)
                                                        if (emp != null) {
                                                            employeeRepository.updateEmployee(emp.copy(photoUri = file.absolutePath))
                                                        }

                                                        Toast.makeText(context, "Face Enrolled Successfully!", Toast.LENGTH_SHORT).show()
                                                        onEnrollSuccess()
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("FaceEnroll", "Error capturing face", e)
                                                    scope.launch {
                                                        Toast.makeText(context, "Failed to capture face", Toast.LENGTH_SHORT).show()
                                                        message = "Keep your face inside frame. Hold still for few seconds until scan is complete."
                                                    }
                                                }
                                            }
                                        },
                                        onNoFaceDetected = {
                                            if (isCapturing) {
                                                isCapturing = false
                                                scope.launch {
                                                    message = "No face detected. Keep still."
                                                }
                                            } else {
                                                scope.launch {
                                                    message = "No face detected. Keep still."
                                                }
                                            }
                                        },
                                        onError = {
                                            isCapturing = false
                                        }
                                    )
                                )
                            }
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewViewRef = previewView
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent overlay with a clear circular cutout
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Calculate center and radius for the cutout
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2 - 150f // slightly higher
            val radius = canvasWidth * 0.45f // Made circle much larger
            
            // Draw the dark semi-transparent background
            with(drawContext.canvas.nativeCanvas) {
                val checkPoint = saveLayer(null, null)
                
                // Dark background
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight)
                )
                
                // Clear the circle area
                drawCircle(
                    color = Color.Transparent,
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    blendMode = BlendMode.Clear
                )
                
                // Draw the guiding lines/corners
                val lineLength = 80f // Longer corner brackets
                val strokeWidth = 10f // Thicker corner brackets
                val cornerColor = Color(0xFF64B5F6)
            
            // Top Left
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius + lineLength),
                end = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                end = androidx.compose.ui.geometry.Offset(centerX - radius + lineLength, centerY - radius),
                strokeWidth = strokeWidth
            )
            
            // Top Right
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX + radius - lineLength, centerY - radius),
                end = androidx.compose.ui.geometry.Offset(centerX + radius, centerY - radius),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX + radius, centerY - radius),
                end = androidx.compose.ui.geometry.Offset(centerX + radius, centerY - radius + lineLength),
                strokeWidth = strokeWidth
            )
            
            // Bottom Left
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX - radius, centerY + radius - lineLength),
                end = androidx.compose.ui.geometry.Offset(centerX - radius, centerY + radius),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX - radius, centerY + radius),
                end = androidx.compose.ui.geometry.Offset(centerX - radius + lineLength, centerY + radius),
                strokeWidth = strokeWidth
            )
            
            // Bottom Right
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX + radius - lineLength, centerY + radius),
                end = androidx.compose.ui.geometry.Offset(centerX + radius, centerY + radius),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = cornerColor,
                start = androidx.compose.ui.geometry.Offset(centerX + radius, centerY + radius),
                end = androidx.compose.ui.geometry.Offset(centerX + radius, centerY + radius - lineLength),
                strokeWidth = strokeWidth
            )
            // Center horizontal guide line
            drawLine(
                color = cornerColor.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(centerX - radius - 20f, centerY),
                end = androidx.compose.ui.geometry.Offset(centerX + radius + 20f, centerY),
                strokeWidth = 3f
            )

            restoreToCount(checkPoint)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isCapturing = true
                    message = "Detecting face..."
                },
                enabled = !isCapturing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture & Save Face")
            }
        }
    }
}
