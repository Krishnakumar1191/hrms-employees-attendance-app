package com.example.hr_employeemanagementsystem.ml

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FaceDetectorManager(
    private val onFrameAnalyzed: (ImageProxy) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        try {
            onFrameAnalyzed(imageProxy)
        } catch (e: Exception) {
            onError(e)
            imageProxy.close()
        }
    }
}