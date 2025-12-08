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

data class ProductRatingStats(
    val _id: String,
    val averageRating: Double,
    val count: Int
)