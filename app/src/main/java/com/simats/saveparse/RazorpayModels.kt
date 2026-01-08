package com.simats.saveparse

import com.google.gson.annotations.SerializedName

// Razorpay Order Creation Response
data class RazorpayOrderResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("order_id") val orderId: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("amount_in_paise") val amountInPaise: Int? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("key_id") val keyId: String? = null,
    @SerializedName("donation_id") val donationId: Int? = null,
    @SerializedName("center_name") val centerName: String? = null,
    @SerializedName("receipt") val receipt: String? = null
)

// Razorpay Payment Verification Response
data class RazorpayVerifyResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("transaction_id") val transactionId: String? = null
)
