package com.simats.saveparse

data class LoginResponse(
    val status: String,
    val message: String,
    val user: User?
)
