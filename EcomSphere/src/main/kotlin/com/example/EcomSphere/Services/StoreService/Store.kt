package com.example.EcomSphere.Services.StoreService

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("stores")
data class Store (
    @Id val id: String? = null,
    @Indexed(unique = true) var name: String,
    val description: String,
    val address: String?,
    val owner: String,
    val phoneNumber: String
){
}