package com.simats.saveparse

import com.google.gson.annotations.SerializedName

// Response for donation list
data class DonationListResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("total_donations") val totalDonations: Int,
    val donations: List<DonationItem>
)

// Single donation item in list
data class DonationItem(
    @SerializedName("donation_id") val donationId: Int,
    @SerializedName("center_id") val centerId: Int,
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("image_of_animal") val imageOfAnimal: String?,
    val amount: Double,
    @SerializedName("requested_time") val requestedTime: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("center_phone") val centerPhone: String?,
    @SerializedName("animal_type") val animalType: String?,
    @SerializedName("animal_condition") val animalCondition: String?,
    @SerializedName("case_photo") val casePhoto: String?
)

// Response for donation details
data class DonationDetailsResponse(
    val success: Boolean,
    val message: String,
    val donation: DonationDetails?
)

// Full donation details
data class DonationDetails(
    @SerializedName("donation_id") val donationId: Int,
    @SerializedName("center_id") val centerId: Int,
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("image_of_animal") val imageOfAnimal: String?,
    val amount: Double,
    @SerializedName("requested_time") val requestedTime: String?,
    @SerializedName("approval_status") val approvalStatus: String?,
    @SerializedName("donation_status") val donationStatus: String?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("transaction_id") val transactionId: String?,
    @SerializedName("payment_time") val paymentTime: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("center_phone") val centerPhone: String?,
    @SerializedName("center_address") val centerAddress: String?,
    @SerializedName("center_email") val centerEmail: String?,
    @SerializedName("animal_type") val animalType: String?,
    @SerializedName("animal_condition") val animalCondition: String?,
    @SerializedName("case_photo") val casePhoto: String?,
    @SerializedName("case_status") val caseStatus: String?
)
