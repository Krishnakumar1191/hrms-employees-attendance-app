package com.example.hr_employeemanagementsystem.ml

import com.example.hr_employeemanagementsystem.data.model.EmployeeEntity

class FaceMatcher {

    /**
     * Matches a live face embedding against enrolled employee face descriptors in Room DB.
     * Threshold = 0.70 similarity for cosine metric.
     */
    fun matchFace(liveEmbedding: FloatArray, enrolledEmployees: List<EmployeeEntity>): EmployeeEntity? {
        var bestMatch: EmployeeEntity? = null
        var highestScore = 0.70f // Threshold cutoff

        for (employee in enrolledEmployees) {
            val descriptorStr = employee.faceDescriptor ?: continue
            val enrolledEmbedding = EmbeddingUtils.stringToFloatArray(descriptorStr) ?: continue

            val similarity = EmbeddingUtils.calculateCosineSimilarity(liveEmbedding, enrolledEmbedding)
            if (similarity > highestScore) {
                highestScore = similarity
                bestMatch = employee
            }
        }
        return bestMatch
    }
}
