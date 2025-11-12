package com.example.EcomSphere.Services.UserService

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class User(
    @Id val id: String? = null,
    @Indexed(unique = true) val email: String,
    val name: String,
    val passwordHash: String
)