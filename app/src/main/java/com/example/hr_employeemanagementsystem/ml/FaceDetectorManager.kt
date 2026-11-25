package com.example.hr_employeemanagementsystem.ml

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

@ExperimentalGetImage
class FaceDetectorManager(
    private val onFaceDetected: (android.graphics.Rect, android.graphics.Bitmap) -> Unit,
    private val onNoFaceDetected: () -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    // Ultra-strict human face detector requiring open eyes and forward-facing head angles
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.20f) // Face must occupy at least 20% of frame
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    // Rigorous validation to ensure it is a real front-facing human face, not an object or shadow
                    val validFaces = faces.filter { face ->
                        val rightEye = face.rightEyeOpenProbability ?: -1f
                        val leftEye = face.leftEyeOpenProbability ?: -1f
                        val headAngleY = face.headEulerAngleY // Yaw (left/right turn)
                        val headAngleZ = face.headEulerAngleZ // Roll (tilt)

                        // Must have valid eye probabilities and be facing relatively forward
                        rightEye > 0.15f && leftEye > 0.15f &&
                        Math.abs(headAngleY) < 35f && Math.abs(headAngleZ) < 35f &&
                        face.allLandmarks.isNotEmpty()
                    }

                    if (validFaces.isNotEmpty()) {
                        val boundingBox = validFaces.first().boundingBox
                        
                        val originalBitmap = imageProxy.toBitmap()
                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(rotationDegrees.toFloat())

                        val rotatedBitmap = android.graphics.Bitmap.createBitmap(
                            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
                        )

                        onFaceDetected(boundingBox, rotatedBitmap)
                    } else {
                        onNoFaceDetected()
                    }
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    onError(e)
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
