package com.example.EcomSphere.Listeners

import com.example.EcomSphere.Models.Comment
import com.example.EcomSphere.Repositories.CommentRepository
import com.example.EcomSphere.Repositories.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.ReentrantLock

@Component
class CommentEventListener(
    private val productRepository: ProductRepository,
    private val commentRepository: CommentRepository
) : AbstractMongoEventListener<Comment>() {

    private val logger = LoggerFactory.getLogger(CommentEventListener::class.java)
    private val productLocks = mutableMapOf<String, ReentrantLock>()

    @Synchronized
    private fun getLock(productId: String): ReentrantLock {
        return productLocks.getOrPut(productId) { ReentrantLock() }
    }

    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100)
    )
    @Transactional
    override fun onAfterSave(event: AfterSaveEvent<Comment>) {
        val comment = event.source
        try {
            updateProductRating(comment.productId)
        } catch (e: Exception) {
            logger.error("Error updating product rating after save for product ${comment.productId}", e)
            throw e
        }
    }

    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100)
    )
    @Transactional
    override fun onAfterDelete(event: AfterDeleteEvent<Comment>) {
        val commentId = event.source["_id"]?.toString()
        commentId?.let {
            try {
                val comment = commentRepository.findById(it).orElse(null)
                comment?.let { c ->
                    updateProductRating(c.productId)
                }
            } catch (e: Exception) {
                logger.error("Error updating product rating after delete for comment $it", e)
                throw e
            }
        }
    }

    @Synchronized
    fun updateProductRating(productId: String) {
        val lock = getLock(productId)
        if (!lock.tryLock()) {
            logger.warn("Could not acquire lock for product $productId, skipping update")
            return
        }

        try {
            val product = productRepository.findById(productId).orElse(null) ?: run {
                logger.warn("Product $productId not found")
                return
            }

            // Use aggregation for better performance with large datasets
            val stats = commentRepository.getProductRatingStats(productId)

            if (stats != null && stats.count > 0) {
                product.ratingAvg = String.format("%.1f", stats.averageRating).toDouble()
                product.reviewCount = stats.count
            } else {
                product.ratingAvg = 0.0
                product.reviewCount = 0
            }

            productRepository.save(product)
        } catch (e: Exception) {
            logger.error("Error updating product rating for product $productId", e)
            throw e
        } finally {
            lock.unlock()
        }
    }
}

// Add this data class for the aggregation result
data class ProductRatingStats(
    val productId: String,
    val averageRating: Double,
    val count: Int
)