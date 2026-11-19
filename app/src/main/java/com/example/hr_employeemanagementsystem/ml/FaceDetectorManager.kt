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

    // Strict and accurate human face detector to ignore chairs and inanimate objects
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
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
                    // Filter out chairs and non-human objects by validating eye features and landmarks
                    val validFaces = faces.filter { face ->
                        val rightEye = face.rightEyeOpenProbability ?: 0.5f
                        val leftEye = face.leftEyeOpenProbability ?: 0.5f
                        face.allLandmarks.isNotEmpty() && (rightEye > 0.05f || leftEye > 0.05f)
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
