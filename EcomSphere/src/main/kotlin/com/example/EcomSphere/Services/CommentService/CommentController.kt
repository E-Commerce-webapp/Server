package com.example.EcomSphere.Controllers

import com.example.EcomSphere.Services.CommentService.CommentResponse
import com.example.EcomSphere.Services.CommentService.CommentService
import com.example.EcomSphere.Services.CommentService.CreateCommentRequest
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
        val comment = commentService.createComment(request, userDetails.username)
        return ResponseEntity
            .created(URI.create("/api/comments/${comment.id}"))
            .body(comment)
    }

    @GetMapping("/product/{productId}")
    fun getProductComments(@PathVariable productId: String) =
        ResponseEntity.ok(commentService.getCommentsByProduct(productId))

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ) = ResponseEntity.ok().build().also {
        commentService.deleteComment(commentId, userDetails.username)
    }
}