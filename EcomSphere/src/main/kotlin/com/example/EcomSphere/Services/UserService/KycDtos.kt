package com.example.EcomSphere.Services.UserService

data class KycSubmissionRequest(
    val storeName: String,
    val phoneNumber: String,
    val businessAddress: String,
    val businessDescription: String
)

data class KycVerificationResponse(
    val success: Boolean,
    val message: String
)
