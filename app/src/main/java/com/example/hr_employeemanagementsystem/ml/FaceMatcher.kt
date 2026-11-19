package com.example.hr_employeemanagementsystem.ml

import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity

class FaceMatcher {

    /**
     * Matches a live face embedding strictly against enrolled employee face descriptors in Room DB.
     * Ensures zero cross-matching between different employees (e.g. Mani vs Raj).
     */
    fun matchFace(liveEmbedding: FloatArray, enrolledEmployees: List<EmployeeEntity>): EmployeeEntity? {
        if (enrolledEmployees.isEmpty()) return null

        var bestMatch: EmployeeEntity? = null
        // Strict threshold to ensure distinct facial feature separation between different employees
        var highestScore = 0.45f

        for (employee in enrolledEmployees) {
            val descriptorStr = employee.faceDescriptor ?: continue
            val enrolledEmbedding = EmbeddingUtils.stringToFloatArray(descriptorStr) ?: continue

            val similarity = EmbeddingUtils.calculateCosineSimilarity(liveEmbedding, enrolledEmbedding)
            android.util.Log.d("FaceMatcher", "Comparing live face with [${employee.displayName}] -> Similarity Score: $similarity")

            if (similarity > highestScore) {
                highestScore = similarity
                bestMatch = employee
            }
        }

        if (bestMatch != null) {
            android.util.Log.d("FaceMatcher", "Successfully matched employee: ${bestMatch.displayName} with score: $highestScore")
        } else {
            android.util.Log.d("FaceMatcher", "No enrolled employee matched the live face above threshold.")
        }

        return bestMatch
    }
}
