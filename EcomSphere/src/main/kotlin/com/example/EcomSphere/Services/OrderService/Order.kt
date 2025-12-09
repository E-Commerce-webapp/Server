package com.example.EcomSphere.Services.OrderService

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("orders")
data class Order(
    @Id val id: String? = null,
    val userId: String,
    val storeId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val shippingAddress: ShippingAddress,
    val paymentMethod: String,
    val shippingCost: Double = 0.0,
    val taxAmount: Double = 0.0,
    val orderNumber: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItem(
    val productId: String,
    val productTitle: String,
    val productImage: String,
    val quantity: Int,
    val price: Double,
    val sellerId: String
)

data class ShippingAddress(
    val fullName: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val postalCode: String,
    val country: String,
    val phoneNumber: String
)

enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
