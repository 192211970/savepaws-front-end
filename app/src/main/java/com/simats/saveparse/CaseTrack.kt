package com.simats.saveparse

import com.google.gson.annotations.SerializedName

/**
 * Response model for case tracking API
 */
data class CaseTrackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("case_info") val caseInfo: CaseInfo?,
    @SerializedName("rescue_info") val rescueInfo: RescueInfo?,
    @SerializedName("escalations") val escalations: List<Escalation>,
    @SerializedName("timeline") val timeline: List<TimelineItem>
)

/**
 * Basic case information
 */
data class CaseInfo(
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("photo") val photo: String,
    @SerializedName("type_of_animal") val typeOfAnimal: String,
    @SerializedName("animal_condition") val animalCondition: String,
    @SerializedName("case_status") val caseStatus: String,
    @SerializedName("created_time") val createdTime: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("reported_by") val reportedBy: String?
)

/**
 * Rescue operation information
 */
data class RescueInfo(
    @SerializedName("rescue_center") val rescueCenter: String?,
    @SerializedName("rescue_center_phone") val rescueCenterPhone: String?,
    @SerializedName("rescue_status") val rescueStatus: String?,
    @SerializedName("rescue_photo") val rescuePhoto: String?,
    @SerializedName("closed_time") val closedTime: String?
)

/**
 * Escalation history item
 */
data class Escalation(
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("response") val response: String?,
    @SerializedName("rejected_reason") val rejectedReason: String?,
    @SerializedName("assigned_time") val assignedTime: String?,
    @SerializedName("responded_time") val respondedTime: String?
)

/**
 * Timeline step item
 */
data class TimelineItem(
    @SerializedName("step") val step: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("status") val status: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("rescue_photo") val rescuePhoto: String? = null
)
