package com.example.EcomSphere.Services.OrderService

import com.example.EcomSphere.Helper.ForbiddenActionException
import com.example.EcomSphere.Services.ProductService.ProductRepository
import com.example.EcomSphere.Services.StoreService.StoreRepository
import com.example.EcomSphere.Services.UserService.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository
) {

    fun createOrder(userId: String, request: CreateOrderRequest): OrderResponse {
        // Validate user exists
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Validate products exist and have sufficient stock
        request.items.forEach { item ->
            val product = productRepository.findById(item.productId)
                .orElseThrow { IllegalArgumentException("Product ${item.productId} not found") }
            
            if (product.stock < item.quantity) {
                throw IllegalArgumentException("Insufficient stock for product: ${product.title}")
            }
        }

        // Calculate total amount
        val itemsTotal = request.items.sumOf { it.price * it.quantity }
        val totalAmount = itemsTotal + request.shippingCost + request.taxAmount

        // Determine store ID (for now, use the seller ID of the first item)
        val storeId = request.items.firstOrNull()?.sellerId ?: ""

        // Generate order number
        val orderNumber = generateOrderNumber()

        // Create order
        val order = Order(
            userId = userId,
            storeId = storeId,
            items = request.items.map { 
                OrderItem(
                    productId = it.productId,
                    productTitle = it.productTitle,
                    productImage = it.productImage,
                    quantity = it.quantity,
                    price = it.price,
                    sellerId = it.sellerId
                )
            },
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
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
            shippingCost = request.shippingCost,
            taxAmount = request.taxAmount,
            orderNumber = orderNumber
        )

        val savedOrder = orderRepository.save(order)

        // Update product stock
        request.items.forEach { item ->
            val product = productRepository.findById(item.productId).get()
            val updatedProduct = product.copy(stock = product.stock - item.quantity)
            productRepository.save(updatedProduct)
        }

        return savedOrder.toResponse()
    }

    fun getOrderById(orderId: String, userId: String): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        // Verify user owns this order or is the seller
        if (order.userId != userId && !isSellerOfOrder(order, userId)) {
            throw ForbiddenActionException("You don't have permission to view this order")
        }

        return order.toResponse()
    }

    fun getUserOrders(userId: String): List<OrderResponse> {
        val orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
        return orders.map { it.toResponse() }
    }

    fun getSellerOrders(sellerId: String): List<OrderResponse> {
        // Get all orders where the seller has products
        val allOrders = orderRepository.findAll()
        val sellerOrders = allOrders.filter { order ->
            order.items.any { it.sellerId == sellerId }
        }
        return sellerOrders.map { it.toResponse() }
    }

    fun updateOrderStatus(orderId: String, userId: String, request: UpdateOrderStatusRequest): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        // Verify user is the seller of products in this order
        if (!isSellerOfOrder(order, userId)) {
            throw ForbiddenActionException("Only the seller can update order status")
        }

        val updatedOrder = order.copy(
            status = request.status,
            updatedAt = LocalDateTime.now()
        )

        val savedOrder = orderRepository.save(updatedOrder)
        return savedOrder.toResponse()
    }

    fun cancelOrder(orderId: String, userId: String): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        // Verify user owns this order
        if (order.userId != userId) {
            throw ForbiddenActionException("You can only cancel your own orders")
        }

        // Only allow cancellation if order is PENDING or PROCESSING
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PROCESSING) {
            throw IllegalStateException("Order cannot be cancelled in ${order.status} status")
        }

        val updatedOrder = order.copy(
            status = OrderStatus.CANCELLED,
            updatedAt = LocalDateTime.now()
        )

        val savedOrder = orderRepository.save(updatedOrder)

        // Restore product stock
        order.items.forEach { item ->
            val product = productRepository.findById(item.productId).orElse(null)
            if (product != null) {
                val updatedProduct = product.copy(stock = product.stock + item.quantity)
                productRepository.save(updatedProduct)
            }
        }

        return savedOrder.toResponse()
    }

    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAll().map { it.toResponse() }
    }

    private fun isSellerOfOrder(order: Order, userId: String): Boolean {
        return order.items.any { it.sellerId == userId }
    }

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random.nextInt(1000, 9999)
        return "ORD-$timestamp-$random"
    }
}
