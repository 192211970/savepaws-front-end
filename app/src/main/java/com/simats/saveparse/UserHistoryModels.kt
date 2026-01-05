package com.simats.saveparse

import com.google.gson.annotations.SerializedName

data class UserCasesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("total_cases") val totalCases: Int,
    @SerializedName("cases") val cases: List<UserCase>
)

data class UserCase(
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("photo") val photo: String?,
    @SerializedName("animal_type") val animalType: String,
    @SerializedName("condition") val condition: String,
    @SerializedName("status") val status: String,
    @SerializedName("case_progress") val caseProgress: String?,
    @SerializedName("center_name") val centerName: String?,
    @SerializedName("reported_time") val reportedTime: String
)

data class UserDonationsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("total_donations") val totalDonations: Int,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("donations") val donations: List<UserDonation>
)

data class UserDonation(
    @SerializedName("donation_id") val donationId: Int,
    @SerializedName("case_id") val caseId: Int,
    @SerializedName("image") val image: String?,
    @SerializedName("amount") val amount: Double,
    @SerializedName("animal_type") val animalType: String,
    @SerializedName("center_name") val centerName: String,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("transaction_id") val transactionId: String?,
    @SerializedName("payment_time") val paymentTime: String?,
    @SerializedName("status") val status: String
)
