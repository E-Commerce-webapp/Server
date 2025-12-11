package com.example.EcomSphere.Services.OrderService

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun createOrder(userId: String, request: CreateOrderRequest): Order {
        val subtotal = request.items.sumOf { it.price * it.quantity }
        val totalAmount = subtotal + request.shippingCost + request.taxAmount

        val order = Order(
            userId = userId,
            items = request.items.map { item ->
                OrderItem(
                    productId = item.productId,
                    productTitle = item.productTitle,
                    productImage = item.productImage,
                    quantity = item.quantity,
                    price = item.price,
                    sellerId = item.sellerId
                )
            },
            shippingAddress = ShippingAddress(
                fullName = request.shippingAddress.fullName,
                addressLine1 = request.shippingAddress.addressLine1,
                addressLine2 = request.shippingAddress.addressLine2,
                city = request.shippingAddress.city,
                postalCode = request.shippingAddress.postalCode,
                country = request.shippingAddress.country,
                phoneNumber = request.shippingAddress.phoneNumber
            ),
            paymentMethod = request.paymentMethod,
            subtotal = subtotal,
            shippingCost = request.shippingCost,
            taxAmount = request.taxAmount,
            totalAmount = totalAmount
        )

        return orderRepository.save(order)
    }

    fun getOrderById(orderId: String): Order? {
        return orderRepository.findById(orderId).orElse(null)
    }

    fun getOrdersByUserId(userId: String): List<Order> {
        return orderRepository.findByUserId(userId)
    }

    fun getOrdersBySellerId(sellerId: String): List<Order> {
        return orderRepository.findByItemsSellerId(sellerId)
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus): Order? {
        val order = orderRepository.findById(orderId).orElse(null) ?: return null
        
        val updatedOrder = order.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        
        return orderRepository.save(updatedOrder)
    }

    fun cancelOrder(orderId: String, userId: String): Order? {
        val order = orderRepository.findById(orderId).orElse(null) ?: return null
        
        // Only allow cancellation if order belongs to user and is in cancellable state
        if (order.userId != userId) {
            throw IllegalArgumentException("Order does not belong to this user")
        }
        
        if (order.status !in listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED)) {
            throw IllegalArgumentException("Order cannot be cancelled in current status: ${order.status}")
        }
        
        val cancelledOrder = order.copy(
            status = OrderStatus.CANCELLED,
            updatedAt = LocalDateTime.now()
        )
        
        return orderRepository.save(cancelledOrder)
    }

    fun Order.toResponse(): OrderResponse {
        return OrderResponse(
            id = this.id!!,
            userId = this.userId,
            items = this.items,
            shippingAddress = this.shippingAddress,
            paymentMethod = this.paymentMethod,
            status = this.status.name,
            subtotal = this.subtotal,
            shippingCost = this.shippingCost,
            taxAmount = this.taxAmount,
            totalAmount = this.totalAmount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
