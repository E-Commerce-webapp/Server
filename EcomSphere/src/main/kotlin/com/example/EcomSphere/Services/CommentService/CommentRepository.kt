package com.example.EcomSphere.Services.CommentService

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository : MongoRepository<Comment, String> {
    fun findByProductIdOrderByCreatedAtDesc(productId: String): List<Comment>
    fun findByProductIdAndUserId(productId: String, userId: String): Comment?

    @Aggregation(pipeline = [
        "{'\$match': {'productId': ?0}}",
        "{'\$group': {_id: '\$productId', averageRating: {'\$avg': '\$rating'}, count: {'\$sum': 1}}}"
    ])
    fun getProductRatingStats(productId: String): ProductRatingStats?
}

/**
 * Data class representing the result of the product rating statistics aggregation.
 * Field names must match the MongoDB aggregation result fields exactly.
 */
data class ProductRatingStats(
    val _id: String,           // Matches the _id field from $group stage
    val averageRating: Double,  // Matches the averageRating field from $group stage
    val count: Int              // Matches the count field from $group stage
)