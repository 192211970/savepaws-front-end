package com.simats.saveparse

// Admin Dashboard Stats Response
data class AdminDashboardStats(
    val total_cases: Int = 0,
    val pending_cases: Int = 0,
    val in_progress_cases: Int = 0,
    val closed_cases: Int = 0,
    val total_centers: Int = 0,
    val active_centers: Int = 0,
    val total_donations: Int = 0,
    val pending_donations: Int = 0,
    val approved_donations: Int = 0,
    val rejected_donations: Int = 0
)

data class AdminDashboardResponse(
    val success: Boolean,
    val stats: AdminDashboardStats?
)

// Admin Cases Response
data class AdminCase(
    val case_id: Int,
    val user_id: Int?,
    val type_of_animal: String?,
    val animal_condition: String?,
    val photo: String?,
    val latitude: String?,
    val longitude: String?,
    val status: String?,
    val created_time: String?,
    val center_name: String?
)

data class AdminCasesResponse(
    val success: Boolean,
    val cases: List<AdminCase>?
)

// Admin Centers Response
data class AdminCenter(
    val center_id: Int,
    val center_name: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val is_active: Int?,
    val created_at: String?,
    val total_cases_handled: Int?
)

data class AdminCentersResponse(
    val success: Boolean,
    val centers: List<AdminCenter>?
)

// Admin Donations Response
data class AdminDonation(
    val donation_id: Int,
    val case_id: Int?,
    val center_id: Int?,
    val center_name: String?,
    val amount: Double?,
    val requested_time: String?,
    val approval_status: String?,
    val donation_status: String?,
    val type_of_animal: String?,
    val photo: String?
)

data class AdminDonationsResponse(
    val success: Boolean,
    val donations: List<AdminDonation>?
)

// Simple response for actions
data class AdminActionResponse(
    val status: String?,
    val message: String?
)

// =============== PENDING CASES ===============
data class AdminPendingCase(
    val case_id: Int,
    val user_id: Int?,
    val type_of_animal: String?,
    val animal_condition: String?,
    val photo: String?,
    val case_type: String?,  // Critical or Standard
    val case_status: String?,
    val latitude: String?,
    val longitude: String?,
    val created_time: String?,
    val case_age_minutes: Int?,
    val centers_escalated: List<Int>?,
    val remark: String?  // None, Sent_again, Delayed, Rejected_by_all
)

data class AdminPendingCasesResponse(
    val success: Boolean,
    val total_pending: Int?,
    val critical_count: Int?,
    val standard_count: Int?,
    val critical_cases: List<AdminPendingCase>?,
    val standard_cases: List<AdminPendingCase>?
)

// =============== IN PROGRESS CASES ===============
data class AdminInProgressCase(
    val case_id: Int,
    val center_id: Int?,
    val center_name: String?,
    val case_took_up_time: String?,
    val rescue_status: String?,
    val type_of_animal: String?,
    val animal_condition: String?,
    val photo: String?,
    val latitude: String?,
    val longitude: String?,
    val created_time: String?
)

data class AdminInProgressCasesResponse(
    val success: Boolean,
    val total_in_progress: Int?,
    val cases: List<AdminInProgressCase>?
)

// =============== CLOSED CASES ===============
data class AdminClosedCase(
    val case_id: Int,
    val center_id: Int?,
    val center_name: String?,
    val case_took_up_time: String?,
    val rescue_status: String?,
    val rescued_photo: String?,
    val type_of_animal: String?,
    val animal_condition: String?,
    val original_photo: String?,
    val latitude: String?,
    val longitude: String?,
    val created_time: String?
)

data class AdminClosedCasesResponse(
    val success: Boolean,
    val total_closed: Int?,
    val cases: List<AdminClosedCase>?
)

// =============== ADMIN CENTER DETAILS ===============
data class AdminCenterListItem(
    val center_id: Int,
    val center_name: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val is_active: String?,
    val center_status: String?,
    val created_at: String?,
    val cases_handled: Int?
)

data class AdminCentersListResponse(
    val success: Boolean,
    val total_centers: Int?,
    val centers: List<AdminCenterListItem>?
)

data class AdminCenterDetail(
    val center_id: Int,
    val center_name: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val latitude: String?,
    val longitude: String?,
    val is_active: String?,
    val center_status: String?,
    val avg_response_time: Int?,
    val member_since: String?
)

data class AdminCenterStats(
    val cases_handled: Int?,
    val donation_count: Int?,
    val total_donation_amount: Double?
)

data class AdminCenterRecentCase(
    val case_id: Int,
    val type_of_animal: String?,
    val animal_condition: String?,
    val photo: String?,
    val case_took_up_time: String?,
    val rescue_status: String?
)

data class AdminCenterDetailResponse(
    val success: Boolean,
    val center: AdminCenterDetail?,
    val stats: AdminCenterStats?,
    val recent_cases: List<AdminCenterRecentCase>?
)

data class AdminCenterStatusResponse(
    val status: String?,
    val message: String?,
    val center_id: Int?,
    val center_status: String?,
    val is_active: String?
)

// =============== ADMIN DONATIONS ===============
data class AdminDonationListItem(
    val donation_id: Int,
    val center_id: Int?,
    val case_id: Int?,
    val center_name: String?,
    val amount: Double?,
    val requested_time: String?,
    val user_id: Int?,
    val donor_name: String?,
    val payment_time: String?
)

data class AdminDonationsListResponse(
    val success: Boolean,
    val status: String?,
    val total: Int?,
    val donations: List<AdminDonationListItem>?
)

data class AdminDonationDetail(
    val donation_id: Int,
    val center_id: Int?,
    val case_id: Int?,
    val center_name: String?,
    val center_phone: String?,
    val amount: Double?,
    val image_of_animal: String?,
    val case_photo: String?,
    val type_of_animal: String?,
    val requested_time: String?,
    val approval_status: String?,
    val donation_status: String?,
    val user_id: Int?,
    val donor_name: String?,
    val payment_time: String?,
    val payment_method: String?,
    val transaction_id: String?
)

data class AdminDonationDetailResponse(
    val success: Boolean,
    val donation: AdminDonationDetail?
)

