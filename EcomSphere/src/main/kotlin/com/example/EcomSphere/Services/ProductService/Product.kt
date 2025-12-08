package com.example.EcomSphere.Services.ProductService

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.annotation.Id

@Document("products")
data class Product(
    @Id val id: String? = null,
    val title: String,
    val description: String,
    val category: String,
    val price: Float,
    val stock: Int,
    val images: String,
    val sellerId: String,
    
    @Field("rating_avg")
    var ratingAvg: Double = 0.0,
    
    @Field("review_count")
    var reviewCount: Int = 0
)


