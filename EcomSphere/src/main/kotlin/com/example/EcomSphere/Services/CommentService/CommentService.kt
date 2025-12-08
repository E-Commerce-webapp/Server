package com.example.EcomSphere.Services.CommentService

import com.example.EcomSphere.Helper.ForbiddenActionException
import com.example.EcomSphere.Helper.ResourceNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val productRepository: com.example.EcomSphere.Services.ProductService.ProductRepository
) {
    @Transactional
    fun createComment(request: CreateCommentRequest, userId: String): CommentResponse {
        // Validate product exists
        val product = productRepository.findByIdOrNull(request.productId)
            ?: throw ResourceNotFoundException("Product not found with id: ${request.productId}")

        // Check for existing review by this user
        val existingComment = commentRepository.findByProductIdAndUserId(request.productId, userId)
        if (existingComment != null) {
            throw ForbiddenActionException("You have already reviewed this product")
        }

        // Create and save the comment
        val comment = Comment(
            productId = request.productId,
            userId = userId,
            userFullName = request.userFullName,
            content = request.content,
            rating = request.rating
        )

        val savedComment = commentRepository.save(comment)
        return savedComment.toResponse()
    }

    @Transactional(readOnly = true)
    fun getCommentsByProduct(productId: String): CommentListResponse {
        // Validate product exists
        if (!productRepository.existsById(productId)) {
            throw ResourceNotFoundException("Product not found with id: $productId")
        }

        val comments = commentRepository.findByProductIdOrderByCreatedAtDesc(productId)
            .map { it.toResponse() }

        val stats = commentRepository.getProductRatingStats(productId)
            ?: return CommentListResponse(emptyList(), 0, 0.0)

        return CommentListResponse(
            comments = comments,
            total = stats.count,
            averageRating = String.format("%.1f", stats.averageRating).toDouble()
        )
    }

    @Transactional
    fun deleteComment(commentId: String, userId: String) {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw ResourceNotFoundException("Comment not found with id: $commentId")

        // Check if the user is the owner of the comment
        if (comment.userId != userId) {
            throw ForbiddenActionException("You are not authorized to delete this comment")
        }

        commentRepository.deleteById(commentId)
    }

    private fun Comment.toResponse() = CommentResponse(
        id = id!!,
        productId = productId,
        userId = userId,
        userFullName = userFullName,
        content = content,
        rating = rating,
        createdAt = createdAt
    )
}