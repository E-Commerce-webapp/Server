package com.example.EcomSphere.Services.ProductService

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : MongoRepository<Product, String> {
    // Add any custom query methods if needed
    fun findBySellerId(sellerId: String): List<Product>
}