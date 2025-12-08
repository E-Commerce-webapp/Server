package com.example.EcomSphere.Services.CommentService

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "comments")
data class Comment(
    @Id
    val id: String? = null,
    val productId: String,
    val userId: String,
    val userFullName: String,
    val content: String,
    val rating: Int = 5, // 1-5 stars
    val createdAt: LocalDateTime = LocalDateTime.now()
)