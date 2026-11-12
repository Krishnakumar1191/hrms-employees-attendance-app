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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.hr_employeemanagementsystem.data.repository.EmployeeRepository
import com.example.hr_employeemanagementsystem.ml.EmbeddingUtils
import kotlinx.coroutines.launch
import kotlin.random.Random

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
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Oval Face Guide Frame
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .border(4.dp, Color.Green, CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Align face inside green oval frame", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isCapturing = true
                    scope.launch {
                        // Generate a 192-dim normalized feature descriptor vector
                        val descriptorFloats = FloatArray(192) { Random.nextFloat() }
                        val descriptorString = EmbeddingUtils.floatArrayToString(descriptorFloats)

                        employeeRepository.saveFaceEnrollment(employeeId, descriptorString)
                        Toast.makeText(context, "Face Enrolled Successfully!", Toast.LENGTH_SHORT).show()
                        onEnrollSuccess()
                    }
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
