package com.example.EcomSphere.Services.UserService

data class CreateUserRequest(val firstName: String, val address: String, val phone: String, val email: String, val lastName: String)
data class GetUsersResponse(
    val firstName: String, 
    val lastName: String, 
    val email: String, 
    val id: String?, 
    val isASeller: Boolean, 
    val emailConfirm: Boolean,
    val address: String
)