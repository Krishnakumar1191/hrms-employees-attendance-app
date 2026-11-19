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
        
        var dotProduct = 0.0
        var normV1 = 0.0
        var normV2 = 0.0
        
        for (i in v1.indices) {
            val a = v1[i].toDouble()
            val b = v2[i].toDouble()
            dotProduct += a * b
            normV1 += a * a
            normV2 += b * b
        }
        
        val denominator = Math.sqrt(normV1) * Math.sqrt(normV2)
        if (denominator == 0.0) return 0f
        
        // Ensure result is between 0.0 and 1.0 (often cosine sim can go to -1, but for normed vectors, usually [0,1])
        val similarity = (dotProduct / denominator).toFloat()
        // Typical Euclidean distance is often used for MobileFaceNet. 
        // L2 distance squared = 2 * (1 - cosine_similarity).
        // Let's just return the raw normalized cosine similarity for matching
        return similarity
    }
}
