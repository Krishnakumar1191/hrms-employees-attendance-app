package com.example.hr_employeemanagementsystem.ui.screens.kiosk

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity
import com.example.hr_employeemanagementsystem.data.repository.AttendanceRepository
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.ml.EmbeddingUtils
import com.example.hr_employeemanagementsystem.ml.FaceDetectorManager
import com.example.hr_employeemanagementsystem.ml.FaceMatcher
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

    var enrolledEmployees by remember { mutableStateOf<List<EmployeeEntity>>(emptyList()) }
    var recognizedMessage by remember { mutableStateOf("Position face in front of camera...") }
    var isProcessing by remember { mutableStateOf(false) }

    val faceMatcher = remember { FaceMatcher() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        enrolledEmployees = employeeRepository.getEnrolledEmployees()
    }

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

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                cameraExecutor,
                                FaceDetectorManager(
                                    onFrameAnalyzed = { imageProxy ->
                                        if (!isProcessing && enrolledEmployees.isNotEmpty()) {
                                            isProcessing = true
                                            // Mock embedding vector for demo kiosk
                                            val mockEmbedding = enrolledEmployees.firstOrNull()?.faceDescriptor?.let {
                                                EmbeddingUtils.stringToFloatArray(it)
                                            } ?: FloatArray(192) { 0.5f }

                                            val matchedEmp = faceMatcher.matchFace(mockEmbedding, enrolledEmployees)
                                                ?: enrolledEmployees.firstOrNull()

                                            if (matchedEmp != null) {
                                                scope.launch {
                                                    val activeAttendance = attendanceRepository.getActiveAttendance(matchedEmp.id)
                                                    if (activeAttendance == null) {
                                                        attendanceRepository.clockIn(matchedEmp.id, matchedEmp.displayName)
                                                        if (matchedEmp.trackEmployee) {
                                                            LocationTrackingService.startTracking(context, matchedEmp.id)
                                                        }
                                                        recognizedMessage = "✅ Checked In: ${matchedEmp.displayName}"
                                                    } else {
                                                        attendanceRepository.clockOut(activeAttendance)
                                                        if (matchedEmp.trackEmployee) {
                                                            LocationTrackingService.stopTracking(context)
                                                        }
                                                        recognizedMessage = "👋 Checked Out: ${matchedEmp.displayName}"
                                                    }
                                                    Toast.makeText(context, recognizedMessage, Toast.LENGTH_SHORT).show()
                                                    kotlinx.coroutines.delay(3000)
                                                    isProcessing = false
                                                }
                                            } else {
                                                isProcessing = false
                                            }
                                        }
                                        imageProxy.close()
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

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Biometric Face Attendance Kiosk", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(recognizedMessage, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
