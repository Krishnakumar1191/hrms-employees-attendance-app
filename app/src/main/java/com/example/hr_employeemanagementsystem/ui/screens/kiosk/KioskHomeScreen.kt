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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
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

    val faceMatcher = remember { FaceMatcher() }
    val faceNet = remember { FaceNetModel(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Per-employee cooldown map (employeeId -> last action timestamp)
    val lastActionMap = remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    val lastErrorTime = remember { mutableStateOf(0L) }
    var showLocationAlert by remember { mutableStateOf(false) }

    if (showLocationAlert) {
        AlertDialog(
            onDismissRequest = { showLocationAlert = false },
            title = { Text("Location Services Disabled") },
            text = { Text("Please enable device location services (GPS) in your phone settings to check in and check out.") },
            confirmButton = {
                Button(onClick = {
                    showLocationAlert = false
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) {
                    Text("Enable Location")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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

                                            if (matchedEmp != null) {
                                                if (!isLocationEnabled(context)) {
                                                    showLocationAlert = true
                                                    isProcessing = false
                                                    return@FaceDetectorManager
                                                }

                                                val lastAction = lastActionMap.value[matchedEmp.id] ?: 0L
                                                val currentTime = System.currentTimeMillis()
                                                val personCooldownMs = 3 * 1000L

                                                if (currentTime - lastAction < personCooldownMs) {
                                                    isProcessing = false
                                                    return@FaceDetectorManager
                                                }

                                                lastActionMap.value = lastActionMap.value + (matchedEmp.id to currentTime)

                                                scope.launch {
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

                                                    val activeAttendance = attendanceRepository.getActiveAttendance(matchedEmp.id)

                                                    if (activeAttendance == null) {
                                                        val (lat, lng) = fetchInstantGpsLocation(context)
                                                        attendanceRepository.clockIn(matchedEmp.id, matchedEmp.displayName, photoUri, matchedEmp.photoUri, lat, lng)
                                                        if (matchedEmp.trackEmployee) {
                                                            LocationTrackingService.startTracking(context, matchedEmp.id)
                                                        }
                                                        recognizedMessage = "✅ Checked In: ${matchedEmp.displayName}"
                                                        Toast.makeText(context, recognizedMessage, Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                                        val checkInTime = try { format.parse(activeAttendance.checkIn)?.time ?: 0L } catch(e: Exception) { 0L }

                                                        val cooldownMs = 15 * 1000L
                                                        if (currentTime - checkInTime < cooldownMs) {
                                                            recognizedMessage = "⏳ Wait a moment to Check Out, ${matchedEmp.displayName}"
                                                        } else {
                                                            val (outLat, outLng) = fetchInstantGpsLocation(context)
                                                            attendanceRepository.clockOut(activeAttendance, outLat, outLng, photoUri)
                                                            if (matchedEmp.trackEmployee) {
                                                                LocationTrackingService.stopTracking(context)
                                                            }
                                                            recognizedMessage = "👋 Checked Out: ${matchedEmp.displayName}"
                                                            Toast.makeText(context, recognizedMessage, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }

                                                    kotlinx.coroutines.delay(1000)
                                                    recognizedMessage = "Position face inside the frame"
                                                    isProcessing = false
                                                }
                                            } else {
                                                val currentTime = System.currentTimeMillis()
                                                if (currentTime - lastErrorTime.value > 2000L) {
                                                    lastErrorTime.value = currentTime
                                                    val hasUnenrolledStaff = allEmployeesState.value.any { !it.isEnrolled || it.faceDescriptor.isNullOrEmpty() }
                                                    recognizedMessage = if (hasUnenrolledStaff) "❌ You are not enrolled, please enroll." else "❌ Face not recognized."
                                                    
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(1000)
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
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("FaceKiosk", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Guide Frame / Scanner Box Mask with Dynamic Glowing Border (Blue when waiting, Green when processing)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxWidth = canvasWidth * 0.7f
            val boxHeight = boxWidth * 1.3f
            val left = (canvasWidth - boxWidth) / 2f
            val top = (canvasHeight - boxHeight) / 2f

            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )

            drawRect(
                color = Color.Transparent,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                blendMode = BlendMode.Clear
            )

            val frameColor = if (isProcessing) Color(0xFF22C55E) else Color(0xFF38BDF8)

            drawRect(
                color = frameColor,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                style = Stroke(width = 6f)
            )
        }

        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
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

fun isLocationEnabled(context: android.content.Context): Boolean {
    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
    return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
           lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
}

suspend fun fetchInstantGpsLocation(context: android.content.Context): Pair<Double, Double> = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
    try {
        val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
        val rsPuramLat = 11.0051 // Exact RS Puram office fallback
        val rsPuramLng = 76.9511

        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    continuation.resumeWith(Result.success(Pair(lastLoc.latitude, lastLoc.longitude)))
                } else {
                    continuation.resumeWith(Result.success(Pair(rsPuramLat, rsPuramLng)))
                }
            }.addOnFailureListener {
                continuation.resumeWith(Result.success(Pair(rsPuramLat, rsPuramLng)))
            }
        } else {
            continuation.resumeWith(Result.success(Pair(rsPuramLat, rsPuramLng)))
        }
    } catch (e: Exception) {
        continuation.resumeWith(Result.success(Pair(11.0051, 76.9511)))
    }
}
