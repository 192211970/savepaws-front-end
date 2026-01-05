package com.simats.saveparse

data class RegisterRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val user_type: String
)

