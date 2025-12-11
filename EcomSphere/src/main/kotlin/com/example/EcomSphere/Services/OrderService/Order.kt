package com.example.EcomSphere.Services.OrderService

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("orders")
data class Order(
    @Id val id: String? = null,
    val userId: String,
    val items: List<OrderItem>,
    val shippingAddress: ShippingAddress,
    val paymentMethod: String,
    val status: OrderStatus = OrderStatus.PENDING,
    val subtotal: Double,
    val shippingCost: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItem(
    val productId: String,
    val productTitle: String,
    val productImage: String?,
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
    val phoneNumber: String? = null
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
