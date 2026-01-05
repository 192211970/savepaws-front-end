package com.simats.saveparse

// Pending case model
data class PendingCase(
    val escalation_id: Int,
    val case_id: Int,
    val case_type: String,
    val photo: String?,
    val type_of_animal: String,
    val animal_condition: String,
    val latitude: String,
    val longitude: String,
    val created_time: String,
    val assigned_time: String,
    val case_age_minutes: Int,
    val remark: String
)

// Accepted case model
data class AcceptedCase(
    val status_id: Int,
    val case_id: Int,
    val photo: String?,
    val type_of_animal: String?,
    val animal_condition: String?,
    val latitude: String?,
    val longitude: String?,
    val created_time: String?,
    val case_took_up_time: String?,
    val reached_location: String?,
    val spot_animal: String?,
    val rescue_photo: String?,
    val rescue_status: String?
)

// API Response for pending cases
data class PendingCasesResponse(
    val status: String,
    val total_pending: Int,
    val critical_count: Int,
    val standard_count: Int,
    val cases: List<PendingCase>
)

// API Response for accepted cases
data class AcceptedCasesResponse(
    val status: String,
    val total_accepted: Int,
    val in_progress_count: Int,
    val closed_count: Int,
    val cases: List<AcceptedCase>
)

// API Response for case actions
data class CaseActionResponse(
    val status: String,
    val message: String
)

// Center info response
data class CenterInfoResponse(
    val status: String,
    val center_id: Int,
    val center_name: String,
    val is_active: String,
    val total_cases_handled: Int
)

// Center donation item for donation status list (different from user's DonationItem)
data class CenterDonationItem(
    val donation_id: Int,
    val case_id: Int,
    val amount: Double?,
    val image_of_animal: String?,
    val requested_time: String?,
    val approval_status: String?,
    val donation_status: String?,
    val user_id: Int?,
    val transaction_id: String?,
    val payment_method: String?,
    val payment_time: String?
)

// Donation history response for center
data class DonationHistoryResponse(
    val status: String,
    val center_id: Int?,
    val total: Int,
    val donations: List<CenterDonationItem>
)

// ============ CENTER PROFILE MODELS ============

// Handled case item for center profile
data class CenterHandledCase(
    val case_id: Int,
    val type_of_animal: String?,
    val animal_condition: String?,
    val photo: String?,
    val case_took_up_time: String?,
    val rescue_status: String?
)

// Received donation item for center profile
data class CenterReceivedDonation(
    val donation_id: Int,
    val case_id: Int,
    val amount: Double?,
    val payment_time: String?,
    val payment_method: String?,
    val transaction_id: String?,
    val donor_name: String?
)

// Center profile info
data class CenterProfileInfo(
    val center_id: Int,
    val center_name: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val is_active: String?,
    val member_since: String?
)

// Center profile stats
data class CenterProfileStats(
    val total_cases_handled: Int,
    val total_donations: Int,
    val total_amount_received: Double
)

// Center profile response
data class CenterProfileResponse(
    val success: Boolean,
    val center: CenterProfileInfo?,
    val stats: CenterProfileStats?,
    val handled_cases: List<CenterHandledCase>?,
    val received_donations: List<CenterReceivedDonation>?
)
