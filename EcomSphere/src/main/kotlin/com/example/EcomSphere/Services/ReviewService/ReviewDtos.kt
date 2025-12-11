package com.example.EcomSphere.Services.ReviewService

data class CreateReviewRequest(
    val productId: String,
    val rating: Int,
    val reviewText: String
)

data class UpdateReviewRequest(
    val rating: Int? = null,
    val reviewText: String? = null
)

data class ReviewResponse(
    val id: String,
    val productId: String,
    val userId: String,
    val rating: Int,
    val reviewText: String,
    val createdAt: String
)

data class AverageRatingResponse(
    val productId: String,
    val averageRating: Double,
    val totalReviews: Int
)
