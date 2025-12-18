package com.example.EcomSphere.Services.StoreService

data class CreateStoreRequest(
    val name: String,
    val phoneNumber: String,
    val description: String,
    val address: String
)

data class CreateStoreResponse(
    val name: String,
    val phoneNumber: String,
    val description: String,
    val address: String,
    val status: StoreStatus = StoreStatus.PENDING
)

data class StoreResponse(
    val name: String,
    val phoneNumber: String,
    val description: String,
    val owner: String,
    val address: String,
    val id: String,
    val status: StoreStatus,
    val avatar: String? = null,
    val cover: String? = null
)

data class UpdateStoreRequest(
    val name: String?,
    val phoneNumber: String?,
    val description: String?,
    val address: String?,
    val status: StoreStatus?,
    val avatar: String?,
    val cover: String?
)