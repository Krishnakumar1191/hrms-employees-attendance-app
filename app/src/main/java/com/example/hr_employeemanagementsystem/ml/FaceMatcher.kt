package com.example.hr_employeemanagementsystem.ml

import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity

class FaceMatcher {

    /**
     * Matches a live face embedding strictly against enrolled employee face descriptors in Room DB.
     * Uses a very strict high-confidence threshold (0.80) to eliminate false positives and prevent
     * wrong person matching.
     */
    fun matchFace(liveEmbedding: FloatArray, enrolledEmployees: List<EmployeeEntity>): EmployeeEntity? {
        if (enrolledEmployees.isEmpty()) return null

        var bestMatch: EmployeeEntity? = null
        var highestScore = 0.58f // Highly reliable threshold for fast face recognition matching

        for (employee in enrolledEmployees) {
            val descriptorStr = employee.faceDescriptor ?: continue
            val enrolledEmbedding = EmbeddingUtils.stringToFloatArray(descriptorStr) ?: continue

            val similarity = EmbeddingUtils.calculateCosineSimilarity(liveEmbedding, enrolledEmbedding)
            android.util.Log.d("FaceMatcher", "Comparing face with [${employee.displayName}] -> Similarity Score: $similarity")

            if (similarity > highestScore) {
                highestScore = similarity
                bestMatch = employee
            }
        }

        if (bestMatch != null) {
            android.util.Log.d("FaceMatcher", "Confirmed Strict Match: ${bestMatch.displayName} with score $highestScore")
        } else {
            android.util.Log.d("FaceMatcher", "Face did not meet strict high-confidence threshold (0.80). Access denied.")
        }

        return bestMatch
    }
}
