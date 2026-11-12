package com.example.hr_employeemanagementsystem.ml

object EmbeddingUtils {

    fun floatArrayToString(array: FloatArray): String {
        return array.joinToString(",")
    }

    fun stringToFloatArray(descriptorString: String): FloatArray? {
        if (descriptorString.isBlank()) return null
        return try {
            descriptorString.split(",").map { it.trim().toFloat() }.toFloatArray()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates Cosine Similarity between two 192-dim embedding vectors.
     * Returns value between 0.0 (unrelated) and 1.0 (exact match).
     */
    fun calculateCosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size || v1.isEmpty()) return 0f
        var dotProduct = 0f
        var normV1 = 0f
        var normV2 = 0f
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            normV1 += v1[i] * v1[i]
            normV2 += v2[i] * v2[i]
        }
        val denominator = Math.sqrt(normV1.toDouble()) * Math.sqrt(normV2.toDouble())
        return if (denominator > 0) (dotProduct / denominator).toFloat() else 0f
    }
}
