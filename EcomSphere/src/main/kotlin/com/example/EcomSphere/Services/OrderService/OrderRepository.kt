package com.example.EcomSphere.Services.OrderService

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : MongoRepository<Order, String> {
    fun findByUserId(userId: String): List<Order>
    fun findByStoreId(storeId: String): List<Order>
    fun findByOrderNumber(orderNumber: String): Order?
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Order>
}
