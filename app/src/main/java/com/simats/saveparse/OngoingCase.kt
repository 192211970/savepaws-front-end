package com.simats.saveparse

import com.google.gson.annotations.SerializedName

/**
 * Response model for ongoing cases API
 */
data class OngoingCasesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("total_cases") val totalCases: Int,
    @SerializedName("cases") val cases: List<OngoingCase>
)

/**
 * Model for a single ongoing case
 */
data class OngoingCase(
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("photo") val photo: String,
    @SerializedName("type_of_animal") val typeOfAnimal: String,
    @SerializedName("animal_condition") val animalCondition: String,
    @SerializedName("case_status") val caseStatus: String,
    @SerializedName("rescue_status") val rescueStatus: String,
    @SerializedName("assigned_center") val assignedCenter: String,
    @SerializedName("created_time") val createdTime: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
