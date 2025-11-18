package com.example.EcomSphere.Services.ProductService

import com.example.EcomSphere.Helper.CustomUserPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
){

    @GetMapping("/external")
    fun getAllProductsExternal(): ResponseEntity<List<GetAllProductsResponse>> {
        val products = productService.getAllProductsExternal()
        return ResponseEntity.ok(products)
    }

    @GetMapping()
    fun getAllProductsInternal(): ResponseEntity<List<GetAllProductsResponse>>{
        val products = productService.getAllProductInternal()
        return ResponseEntity.ok(products)
    }

    @PostMapping()
    fun createProduct(@RequestBody request: CreateProductRequest, authentication: Authentication): ResponseEntity<ProductResponse>{
        val principal = authentication.principal as CustomUserPrincipal
        val sellerId = principal.id
        val created = productService.addProduct(request, sellerId)
        return ResponseEntity.ok(created)
    }

    @PutMapping("/{id}")
    fun updateProduct(@PathVariable id: String, @RequestBody request: UpdateProductRequest, authentication: Authentication): ResponseEntity<ProductResponse>{
        val principal = authentication.principal as CustomUserPrincipal
        val sellerId = principal.id
        val updated = productService.updateProduct(id, request, sellerId)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: String, authentication: Authentication): ResponseEntity<Unit>{
        val principal = authentication.principal as CustomUserPrincipal
        val sellerId = principal.id
        productService.deleteProduct(id, sellerId)
        return ResponseEntity.noContent().build()
    }
}