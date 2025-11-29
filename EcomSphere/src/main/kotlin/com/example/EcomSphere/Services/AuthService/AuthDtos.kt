package com.example.EcomSphere.Services.AuthService

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class EmailVerifyRequest(val email: String)
data class AuthResponse(val token: String)