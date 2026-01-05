package com.simats.saveparse

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("report.php")
    fun reportCase(
        @Part photo: MultipartBody.Part,
        @Part("user_id") userId: RequestBody,
        @Part("type_of_animal") animalType: RequestBody,
        @Part("condition") condition: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("register.php")
    fun registerOrg(
        @Field("name") name: String,
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("user_type") userType: String
    ): Call<RegisterResponse>


    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("get_ongoing_cases.php")
    fun getOngoingCases(
        @Field("user_id") userId: Int
    ): Call<OngoingCasesResponse>

    @FormUrlEncoded
    @POST("get_case_track.php")
    fun getCaseTrack(
        @Field("case_id") caseId: Int
    ): Call<CaseTrackResponse>

    @POST("get_approved_donations.php")
    fun getApprovedDonations(): Call<DonationListResponse>

    @FormUrlEncoded
    @POST("get_donation_details.php")
    fun getDonationDetails(
        @Field("donation_id") donationId: Int
    ): Call<DonationDetailsResponse>

    @FormUrlEncoded
    @POST("payment.php")
    fun processPayment(
        @Field("donation_id") donationId: Int,
        @Field("user_id") userId: Int,
        @Field("transaction_id") transactionId: String,
        @Field("payment_method") paymentMethod: String
    ): Call<PaymentResponse>

    @FormUrlEncoded
    @POST("get_user_cases.php")
    fun getUserCases(
        @Field("user_id") userId: Int
    ): Call<UserCasesResponse>

    @FormUrlEncoded
    @POST("get_user_donations.php")
    fun getUserDonations(
        @Field("user_id") userId: Int
    ): Call<UserDonationsResponse>

    @FormUrlEncoded
    @POST("centerdetails.php")
    fun registerCenter(
        @Field("user_id") userId: Int,
        @Field("center_name") centerName: String,
        @Field("address") address: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("phone") phone: String,
        @Field("email") email: String
    ): Call<CenterDetailsResponse>

    @FormUrlEncoded
    @POST("check_center_details.php")
    fun checkCenterDetails(
        @Field("user_id") userId: Int
    ): Call<CheckCenterResponse>

    // ============ CENTER DASHBOARD APIs ============

    @FormUrlEncoded
    @POST("get_center_pending_cases.php")
    fun getCenterPendingCases(
        @Field("center_id") centerId: Int
    ): Call<PendingCasesResponse>

    @FormUrlEncoded
    @POST("get_center_accepted_cases.php")
    fun getCenterAcceptedCases(
        @Field("center_id") centerId: Int
    ): Call<AcceptedCasesResponse>

    @FormUrlEncoded
    @POST("accrej.php")
    fun respondToCase(
        @Field("center_id") centerId: Int,
        @Field("case_id") caseId: Int,
        @Field("response") response: String,
        @Field("reason") reason: String?
    ): Call<CaseActionResponse>

    @FormUrlEncoded
    @POST("activestatus.php")
    fun updateCenterActiveStatus(
        @Field("center_id") centerId: Int,
        @Field("is_active") isActive: String
    ): Call<CaseActionResponse>

    @FormUrlEncoded
    @POST("reach.php")
    fun markReachedLocation(
        @Field("case_id") caseId: Int,
        @Field("center_id") centerId: Int
    ): Call<CaseActionResponse>

    @FormUrlEncoded
    @POST("spot.php")
    fun markSpottedAnimal(
        @Field("case_id") caseId: Int,
        @Field("center_id") centerId: Int
    ): Call<CaseActionResponse>

    @Multipart
    @POST("close.php")
    fun closeCase(
        @Part("case_id") caseId: RequestBody,
        @Part("center_id") centerId: RequestBody,
        @Part rescue_photo: MultipartBody.Part
    ): Call<CaseActionResponse>

    @Multipart
    @POST("donation.php")
    fun createDonationRequest(
        @Part("center_id") centerId: RequestBody,
        @Part("case_id") caseId: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part image_of_animal: MultipartBody.Part
    ): Call<CaseActionResponse>

    @GET("donation_req_history.php")
    fun getDonationHistory(
        @Query("center_id") centerId: Int
    ): Call<DonationHistoryResponse>

    @GET("get_center_profile.php")
    fun getCenterProfile(
        @Query("center_id") centerId: Int
    ): Call<CenterProfileResponse>

    // =============== ADMIN ENDPOINTS ===============

    @GET("admin_dashboard_stats.php")
    fun getAdminDashboardStats(): Call<AdminDashboardResponse>

    @GET("admin_get_all_cases.php")
    fun getAdminAllCases(
        @Query("status") status: String? = null
    ): Call<AdminCasesResponse>

    @GET("admin_get_all_centers.php")
    fun getAdminAllCenters(): Call<AdminCentersResponse>

    @GET("admin_get_pending_donations.php")
    fun getAdminPendingDonations(): Call<AdminDonationsResponse>

    @FormUrlEncoded
    @POST("donationapproval.php")
    fun approveDonation(
        @Field("donation_id") donationId: Int,
        @Field("action") action: String
    ): Call<AdminActionResponse>

    @FormUrlEncoded
    @POST("admin_toggle_center_status.php")
    fun toggleCenterStatus(
        @Field("center_id") centerId: Int,
        @Field("is_active") isActive: Int
    ): Call<AdminActionResponse>

    @GET("admin_pending_cases.php")
    fun getAdminPendingCases(): Call<AdminPendingCasesResponse>

    @GET("admin_inprogress_cases.php")
    fun getAdminInProgressCases(): Call<AdminInProgressCasesResponse>

    @GET("admin_closed_cases.php")
    fun getAdminClosedCases(): Call<AdminClosedCasesResponse>

}
