package com.example.EcomSphere.Services.CommentService

import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

/**
 * DTO for creating a new comment
 */
data class CreateCommentRequest(
    @field:NotBlank(message = "Content is required")
    @field:Length(min = 10, max = 1000, message = "Content must be between 10 and 1000 characters")
    val content: String,

    @field:NotNull(message = "Rating is required")
    @field:Min(value = 1, message = "Rating must be at least 1")
    @field:Max(value = 5, message = "Rating cannot be more than 5")
    val rating: Int = 5,

    @field:NotBlank(message = "Product ID is required")
    val productId: String,

    @field:NotBlank(message = "User ID is required")
    val userId: String,

    @field:NotBlank(message = "User full name is required")
    val userFullName: String
)

/**
 * DTO representing a comment response
 */
data class CommentResponse(
    val id: String,
    val productId: String,
    val userId: String,
    val userFullName: String,
    val content: String,
    val rating: Int,
    val createdAt: LocalDateTime
)

/**
 * DTO representing a paginated list of comments with statistics
 */
data class CommentListResponse(
    val comments: List<CommentResponse>,
    val total: Int,
    val averageRating: Double
)