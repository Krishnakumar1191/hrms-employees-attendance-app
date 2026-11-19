package com.example.hr_employeemanagementsystem.ui.screens.kiosk

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.core.content.ContextCompat
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.ml.EmbeddingUtils
import com.example.hr_employeemanagementsystem.ml.FaceDetectorManager
import com.example.hr_employeemanagementsystem.ml.FaceMatcher
import com.example.hr_employeemanagementsystem.ml.FaceNetModel
import com.example.hr_employeemanagementsystem.service.LocationTrackingService
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun KioskHomeScreen(
    attendanceRepository: AttendanceRepository,
    employeeRepository: EmployeeRepository
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val allEmployees by employeeRepository.getAllEmployees().collectAsState(initial = emptyList())
    val enrolledEmployees = allEmployees.filter { it.isEnrolled && !it.faceDescriptor.isNullOrEmpty() }

    val enrolledEmployeesState = rememberUpdatedState(enrolledEmployees)
    val allEmployeesState = rememberUpdatedState(allEmployees)

    var recognizedMessage by remember { mutableStateOf("Position face inside the frame") }
    var isProcessing by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val faceMatcher = remember { FaceMatcher() }
    val faceNet = remember { FaceNetModel(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Per-employee cooldown map (employeeId -> last action timestamp)
    val lastActionMap = remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    // Debounce timestamp for error messages to prevent continuous spamming
    val lastErrorTime = remember { mutableStateOf(0L) }

    LaunchedEffect(enrolledEmployees.size) {
        if (enrolledEmployees.isEmpty()) {
            recognizedMessage = "No enrolled faces found. Please enroll employees first."
        } else if (recognizedMessage.contains("No enrolled")) {
            recognizedMessage = "Position face inside the frame"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                cameraExecutor,
                                FaceDetectorManager(
                                    onFaceDetected = { boundingBox, bitmap ->
                                        if (!isProcessing && enrolledEmployeesState.value.isNotEmpty()) {
                                            isProcessing = true
                                            
                                            val liveEmbedding = try {
                                                faceNet.getFaceEmbedding(bitmap, boundingBox)
                                            } catch (e: Exception) {
                                                android.util.Log.e("FaceKiosk", "Error extracting embedding", e)
                                                FloatArray(192) { 0.0f }
                                            }

                                            val matchedEmp = faceMatcher.matchFace(liveEmbedding, enrolledEmployeesState.value)

                                            android.util.Log.d("FaceKiosk", "Extracted Live Embedding: ${liveEmbedding.take(5)}...")
                                            android.util.Log.d("FaceKiosk", "Found matchedEmp: ${matchedEmp?.displayName}")

                                            if (matchedEmp != null) {
                                                val lastAction = lastActionMap.value[matchedEmp.id] ?: 0L
                                                val currentTime = System.currentTimeMillis()
                                                val personCooldownMs = 15 * 1000L // 15 seconds cooldown per employee

                                                if (currentTime - lastAction < personCooldownMs) {
                                                    // Same person standing still, ignore to prevent accidental re-trigger
                                                    isProcessing = false
                                                    return@FaceDetectorManager
                                                }

                                                // Update last action timestamp
                                                lastActionMap.value = lastActionMap.value + (matchedEmp.id to currentTime)

                                                scope.launch {
                                                    val activeAttendance = attendanceRepository.getActiveAttendance(matchedEmp.id)

                                                    if (activeAttendance == null) {
                                                        var photoUri: String? = null
                                                        val file = java.io.File(context.filesDir, "attendance_${matchedEmp.id}_${System.currentTimeMillis()}.jpg")
                                                        try {
                                                            var left = boundingBox.left.coerceAtLeast(0)
                                                            var top = boundingBox.top.coerceAtLeast(0)
                                                            var right = boundingBox.right.coerceAtMost(bitmap.width)
                                                            var bottom = boundingBox.bottom.coerceAtMost(bitmap.height)

                                                            val w = right - left
                                                            val h = bottom - top
                                                            left = (left - w * 0.2f).toInt().coerceAtLeast(0)
                                                            top = (top - h * 0.2f).toInt().coerceAtLeast(0)
                                                            right = (right + w * 0.2f).toInt().coerceAtMost(bitmap.width)
                                                            bottom = (bottom + h * 0.2f).toInt().coerceAtMost(bitmap.height)

                                                            val faceCrop = android.graphics.Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)

                                                            java.io.FileOutputStream(file).use { out ->
                                                                faceCrop.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                                                            }
                                                            photoUri = file.absolutePath
                                                        } catch (e: Exception) {
                                                            android.util.Log.e("FaceKiosk", "Error saving face crop photo", e)
                                                        }

                                                        attendanceRepository.clockIn(matchedEmp.id, matchedEmp.displayName, photoUri, 12.9716, 77.5946)
                                                        if (matchedEmp.trackEmployee) {
                                                            LocationTrackingService.startTracking(context, matchedEmp.id)
                                                        }
                                                        recognizedMessage = "✅ Checked In: ${matchedEmp.displayName}"
                                                        Toast.makeText(context, recognizedMessage, Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                                        val checkInTime = try { format.parse(activeAttendance.checkIn)?.time ?: 0L } catch(e: Exception) { 0L }

                                                        val cooldownMs = 60 * 1000L
                                                        if (currentTime - checkInTime < cooldownMs) {
                                                            recognizedMessage = "⏳ Wait 1 min to Check Out, ${matchedEmp.displayName}"
                                                        } else {
                                                            attendanceRepository.clockOut(activeAttendance)
                                                            if (matchedEmp.trackEmployee) {
                                                                LocationTrackingService.stopTracking(context)
                                                            }
                                                            recognizedMessage = "👋 Checked Out: ${matchedEmp.displayName}"
                                                            Toast.makeText(context, recognizedMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }

                                                    kotlinx.coroutines.delay(2500)
                                                    recognizedMessage = "Position face inside the frame"
                                                    isProcessing = false
                                                }
                                            } else {
                                                val currentTime = System.currentTimeMillis()
                                                if (currentTime - lastErrorTime.value > 3000L) {
                                                    lastErrorTime.value = currentTime
                                                    val hasUnenrolledStaff = allEmployeesState.value.any { !it.isEnrolled || it.faceDescriptor.isNullOrEmpty() }
                                                    
                                                    if (hasUnenrolledStaff) {
                                                        recognizedMessage = "❌ You are not enrolled, please enroll."
                                                    } else {
                                                        recognizedMessage = "❌ You are not an employee for this organization."
                                                    }
                                                    
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(2500)
                                                        recognizedMessage = "Position face inside the frame"
                                                        isProcessing = false
                                                    }
                                                } else {
                                                    isProcessing = false
                                                }
                                            }
                                        }
                                    },
                                    onNoFaceDetected = {
                                        if (!isProcessing) {
                                            recognizedMessage = "Position face inside the frame"
                                        }
                                    },
                                    onError = {
                                        isProcessing = false
                                    }
                                )
                            )
                        }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                    try {
                        cameraProvider.unbindAll()
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

        // Full-screen camera overlay layout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2 - 150f // slightly higher
            val radius = canvasWidth * 0.45f // Made circle much larger
            
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
                
                val lineLength = 80f // Longer corner brackets
                val strokeWidth = 10f // Thicker corner brackets
                val cornerColor = if (isProcessing) Color.Green else Color(0xFF64B5F6)
                
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

        // Top Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CameraFront,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Face Attendance Kiosk",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom Feedback Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (recognizedMessage.contains("✅") || recognizedMessage.contains("👋")) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = recognizedMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
