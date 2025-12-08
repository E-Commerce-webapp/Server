package com.example.EcomSphere.Services.CommentService

import com.example.EcomSphere.Helper.ForbiddenActionException
import com.example.EcomSphere.Helper.ResourceNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/comments")
class CommentController(private val commentService: CommentService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createComment(
        @Valid @RequestBody request: CreateCommentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CommentResponse> {
        // Ensure user information is properly set
        val updatedRequest = request.copy(
            userId = userDetails.username,
            userFullName = userDetails.username // Or get from UserDetails if available
        )
        val comment = commentService.createComment(updatedRequest, userDetails.username)
        return ResponseEntity
            .created(URI.create("/api/comments/${comment.id}"))
            .body(comment)
    }

    @GetMapping("/product/{productId}")
    fun getProductComments(@PathVariable productId: String) =
        try {
            ResponseEntity.ok(commentService.getCommentsByProduct(productId))
        } catch (e: ResourceNotFoundException) {
            ResponseEntity.notFound().build()
        }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(
        @PathVariable commentId: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ) {
        try {
            commentService.deleteComment(commentId, userDetails.username)
        } catch (e: ForbiddenActionException) {
            throw e  // Will be handled by GlobalExceptionHandler
        }
    }
}