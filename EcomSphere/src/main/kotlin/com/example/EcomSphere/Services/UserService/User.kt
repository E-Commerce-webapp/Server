package com.example.EcomSphere.Services.UserService

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class User(
    @Id val id: String? = null,
    @Indexed(unique = true) val email: String,
    var name: String,
    var passwordHash: String,
    var isASeller: Boolean? = null,
    var emailConfirm: Boolean? = null
)