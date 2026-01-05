package com.simats.saveparse

import com.google.gson.annotations.SerializedName

// Payment request response
data class PaymentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("transaction_id") val transactionId: String?
)
