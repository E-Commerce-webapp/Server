package com.example.EcomSphere.Services.OrderService

import com.example.EcomSphere.MiddleWare.JwtUtil
import com.example.EcomSphere.Services.NotificationService.NotificationService
import com.example.EcomSphere.Services.UserService.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    private fun getUserIdFromToken(authHeader: String?): String {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid authorization header")
        }
        val token = authHeader.substring(7)
        val email = jwtUtil.verifyAndGetEmail(token)
            ?: throw IllegalArgumentException("Invalid or expired token")
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("User not found") }
        return user.id!!
    }

    @PostMapping
    fun createOrder(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody request: CreateOrderRequest
    ): ResponseEntity<Any> {
        return try {
            val userId = getUserIdFromToken(authHeader)
            println(">>> Creating order for userId: $userId")
            println(">>> Order items: ${request.items.map { "${it.productTitle} (sellerId=${it.sellerId})" }}")
            val order = orderService.createOrder(userId, request)
            println(">>> Order created with id: ${order.id}")
            
            // Notify sellers about new order
            val buyerName = request.shippingAddress.fullName
            order.items.map { it.sellerId }.distinct().forEach { sellerId ->
                if (sellerId != null) {
                    notificationService.notifyNewOrder(
                        sellerId = sellerId,
                        orderId = order.id!!,
                        orderShortId = order.id.takeLast(8),
                        buyerName = buyerName
                    )
                }
            }
            
            ResponseEntity.status(HttpStatus.CREATED).body(with(orderService) { order.toResponse() })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to create order: ${e.message}"))
        }
    }

    @GetMapping("/{orderId}")
    fun getOrderById(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable orderId: String
    ): ResponseEntity<Any> {
        return try {
            val userId = getUserIdFromToken(authHeader)
            val order = orderService.getOrderById(orderId)
            
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Order not found"))
            }
            
            // Check if user owns this order or is a seller for items in this order
            val isOwner = order.userId == userId
            val isSeller = order.items.any { it.sellerId == userId }
            
            if (!isOwner && !isSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Access denied"))
            }
            
            ResponseEntity.ok(with(orderService) { order.toResponse() })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to fetch order: ${e.message}"))
        }
    }

    @GetMapping("/user")
    fun getUserOrders(
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Any> {
        return try {
            val userId = getUserIdFromToken(authHeader)
            println(">>> Fetching orders for userId: $userId")
            val orders = orderService.getOrdersByUserId(userId)
            println(">>> Found ${orders.size} orders")
            orders.forEachIndexed { index, order ->
                println(">>> Order $index: id=${order.id}, status=${order.status}, items=${order.items.size}")
            }
            val response = orders.map { with(orderService) { it.toResponse() } }
            println(">>> Mapped ${response.size} orders to response")
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            println(">>> IllegalArgumentException: ${e.message}")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            println(">>> Exception: ${e.javaClass.name}: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to fetch orders: ${e.message}"))
        }
    }

    @GetMapping("/seller")
    fun getSellerOrders(
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Any> {
        return try {
            val userId = getUserIdFromToken(authHeader)
            println(">>> Fetching seller orders for userId: $userId")
            val orders = orderService.getOrdersBySellerId(userId)
            println(">>> Found ${orders.size} orders for seller")
            orders.forEach { order ->
                println(">>> Order ${order.id}: items=${order.items.map { "${it.productTitle} (sellerId=${it.sellerId})" }}")
            }
            ResponseEntity.ok(orders.map { with(orderService) { it.toResponse() } })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to fetch seller orders: ${e.message}"))
        }
    }

    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable orderId: String,
        @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<Any> {
        return try {
            val userId = getUserIdFromToken(authHeader)
            val order = orderService.getOrderById(orderId)
            
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Order not found"))
            }
            
            // Only sellers of items in this order can update status
            val isSeller = order.items.any { it.sellerId == userId }
            if (!isSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Only sellers can update order status"))
            }
            
            val status = try {
                OrderStatus.valueOf(request.status.uppercase())
            } catch (e: IllegalArgumentException) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to "Invalid status: ${request.status}"))
            }
            
            val updatedOrder = orderService.updateOrderStatus(orderId, status)
            ResponseEntity.ok(with(orderService) { updatedOrder!!.toResponse() })
        } catch (e: IllegalArgumentException) {
            // Check if it's an auth error or a business logic error
            val message = e.message ?: ""
            if (message.contains("Authorization") || message.contains("token") || message.contains("User not found")) {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to e.message))
            } else {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to update order status: ${e.message}"))
        }
    }

    @PutMapping("/{orderId}/cancel")
    fun cancelOrder(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable orderId: String
    ): ResponseEntity<Any> {
        return try {
            val userId = getUserIdFromToken(authHeader)
            val cancelledOrder = orderService.cancelOrder(orderId, userId)
            
            if (cancelledOrder == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Order not found"))
            }
            
            ResponseEntity.ok(with(orderService) { cancelledOrder.toResponse() })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Failed to cancel order: ${e.message}"))
        }
    }
}
