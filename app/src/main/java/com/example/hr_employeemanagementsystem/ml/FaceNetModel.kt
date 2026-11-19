package com.example.hr_employeemanagementsystem.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class FaceNetModel(context: Context) {
    private val interpreter: Interpreter
    private val imgSize = 112 // MobileFaceNet standard input size
    private val embeddingSize = 192 // Output vector size

    init {
        val modelBuffer = loadModelFile(context, "mobile_face_net.tflite")
        interpreter = Interpreter(modelBuffer, Interpreter.Options().apply { numThreads = 4 })
        val inputShape = interpreter.getInputTensor(0).shape()
        val dataType = interpreter.getInputTensor(0).dataType()
        android.util.Log.d("FaceNetModel", "Input shape: ${inputShape.joinToString()}, Type: $dataType")
    }

    private fun loadModelFile(context: Context, modelName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Extracts a 192-dimensional face embedding from a given face crop.
     */
    fun getFaceEmbedding(bitmap: Bitmap, boundingBox: Rect): FloatArray {
        // Crop face
        var left = boundingBox.left.coerceAtLeast(0)
        var top = boundingBox.top.coerceAtLeast(0)
        var right = boundingBox.right.coerceAtMost(bitmap.width)
        var bottom = boundingBox.bottom.coerceAtMost(bitmap.height)

        // Expand bounding box slightly for better facial feature capture
        val w = right - left
        val h = bottom - top
        
        if (w <= 0 || h <= 0) {
            throw IllegalArgumentException("Invalid bounding box size")
        }
        
        // Increase padding to capture full face properly
        left = (left - w * 0.2f).toInt().coerceAtLeast(0)
        top = (top - h * 0.2f).toInt().coerceAtLeast(0)
        right = (right + w * 0.2f).toInt().coerceAtMost(bitmap.width)
        bottom = (bottom + h * 0.2f).toInt().coerceAtMost(bitmap.height)
        
        val finalW = right - left
        val finalH = bottom - top
        if (finalW <= 0 || finalH <= 0) {
            throw IllegalArgumentException("Invalid cropped dimensions")
        }

        // Crop the upright face
        val croppedFace = Bitmap.createBitmap(bitmap, left, top, finalW, finalH)
        
        // Mirror the face horizontally ONLY AFTER cropping, because front camera is horizontally flipped.
        // If we don't mirror it, the mathematical embedding will be generated in reverse.
        val matrix = android.graphics.Matrix()
        matrix.postScale(-1f, 1f, croppedFace.width / 2f, croppedFace.height / 2f)
        val flippedFace = Bitmap.createBitmap(croppedFace, 0, 0, croppedFace.width, croppedFace.height, matrix, true)

        val scaledFace = Bitmap.createScaledBitmap(flippedFace, imgSize, imgSize, false)

        val inputBuffer = convertBitmapToByteBuffer(scaledFace)
        // The model expects a batch size of 2
        val outputBuffer = Array(2) { FloatArray(embeddingSize) }

        interpreter.run(inputBuffer, outputBuffer)

        // Normalize the embedding (just take the first one since we only care about the single face)
        val embedding = outputBuffer[0]
        var norm = 0f
        for (v in embedding) {
            norm += v * v
        }
        norm = Math.sqrt(norm.toDouble()).toFloat()
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] = embedding[i] / norm
            }
        }
        return embedding
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Model expects batch size of 2, so buffer needs to be exactly twice the size
        val byteBuffer = ByteBuffer.allocateDirect(2 * 4 * imgSize * imgSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imgSize * imgSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Write the first image
        var pixel = 0
        for (i in 0 until imgSize) {
            for (j in 0 until imgSize) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16 and 0xFF) - 127.5f) / 128.0f)
                byteBuffer.putFloat(((value shr 8 and 0xFF) - 127.5f) / 128.0f)
                byteBuffer.putFloat(((value and 0xFF) - 127.5f) / 128.0f)
            }
        }
        
        // Pad the second image with zeros (since we only care about batch index 0)
        for (i in 0 until imgSize * imgSize * 3) {
            byteBuffer.putFloat(0f)
        }
        
        return byteBuffer
    }
}
