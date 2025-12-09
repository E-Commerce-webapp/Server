package com.example.EcomSphere.Services.OrderService

import java.time.LocalDateTime

data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val shippingAddress: ShippingAddressRequest,
    val paymentMethod: String,
    val shippingCost: Double = 0.0,
    val taxAmount: Double = 0.0
)

data class OrderItemRequest(
    val productId: String,
    val productTitle: String,
    val productImage: String,
    val quantity: Int,
    val price: Double,
    val sellerId: String
)

data class ShippingAddressRequest(
    val fullName: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val city: String,
    val postalCode: String,
    val country: String,
    val phoneNumber: String
)

data class UpdateOrderStatusRequest(
    val status: OrderStatus
)

data class OrderResponse(
    val id: String,
    val userId: String,
    val storeId: String,
    val items: List<OrderItemResponse>,
    val totalAmount: Double,
    val status: String,
    val shippingAddress: ShippingAddressResponse,
    val paymentMethod: String,
    val shippingCost: Double,
    val taxAmount: Double,
    val orderNumber: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderItemResponse(
    val productId: String,
    val productTitle: String,
    val productImage: String,
    val quantity: Int,
    val price: Double,
    val sellerId: String
)

data class ShippingAddressResponse(
    val fullName: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val postalCode: String,
    val country: String,
    val phoneNumber: String
)

// Extension functions to convert between entities and DTOs
fun Order.toResponse() = OrderResponse(
    id = id ?: "",
    userId = userId,
    storeId = storeId,
    items = items.map { it.toResponse() },
    totalAmount = totalAmount,
    status = status.name,
    shippingAddress = shippingAddress.toResponse(),
    paymentMethod = paymentMethod,
    shippingCost = shippingCost,
    taxAmount = taxAmount,
    orderNumber = orderNumber,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun OrderItem.toResponse() = OrderItemResponse(
    productId = productId,
    productTitle = productTitle,
    productImage = productImage,
    quantity = quantity,
    price = price,
    sellerId = sellerId
)

fun ShippingAddress.toResponse() = ShippingAddressResponse(
    fullName = fullName,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    postalCode = postalCode,
    country = country,
    phoneNumber = phoneNumber
)
