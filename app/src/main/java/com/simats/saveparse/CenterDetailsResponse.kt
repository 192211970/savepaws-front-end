package com.simats.saveparse

data class CenterDetailsResponse(
    val status: String,
    val message: String,
    val center_id: Int? = null
)

data class CheckCenterResponse(
    val status: String,
    val has_center_details: Boolean,
    val center_id: Int? = null,
    val center_name: String? = null
)
