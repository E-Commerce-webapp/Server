package com.example.EcomSphere.Services.OrderService

import com.example.EcomSphere.Helper.CustomUserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(
        @RequestBody request: CreateOrderRequest,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        val order = orderService.createOrder(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @GetMapping("/{id}")
    fun getOrderById(
        @PathVariable id: String,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        val order = orderService.getOrderById(id, userId)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/user")
    fun getUserOrders(
        authentication: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        val orders = orderService.getUserOrders(userId)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/seller")
    fun getSellerOrders(
        authentication: Authentication
    ): ResponseEntity<List<OrderResponse>> {
        val principal = authentication.principal as CustomUserPrincipal
        val sellerId = principal.id
        val orders = orderService.getSellerOrders(sellerId)
        return ResponseEntity.ok(orders)
    }

    @PutMapping("/{id}/status")
    fun updateOrderStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateOrderStatusRequest,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        val order = orderService.updateOrderStatus(id, userId, request)
        return ResponseEntity.ok(order)
    }

    @PutMapping("/{id}/cancel")
    fun cancelOrder(
        @PathVariable id: String,
        authentication: Authentication
    ): ResponseEntity<OrderResponse> {
        val principal = authentication.principal as CustomUserPrincipal
        val userId = principal.id
        val order = orderService.cancelOrder(id, userId)
        return ResponseEntity.ok(order)
    }

    @GetMapping
    fun getAllOrders(): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getAllOrders()
        return ResponseEntity.ok(orders)
    }
}
