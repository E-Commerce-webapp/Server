package com.example.EcomSphere.Services.MessageService

import com.example.EcomSphere.Services.UserService.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) {
    fun sendMessage(senderId: String, request: SendMessageRequest): Message {
        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("Sender not found") }
        
        val receiver = userRepository.findById(request.receiverId)
            .orElseThrow { IllegalArgumentException("Receiver not found") }
        
        val senderName = "${sender.firstName} ${sender.lastName}"
        
        // Find or create conversation
        val conversation = findOrCreateConversation(
            senderId = senderId,
            receiverId = request.receiverId,
            senderName = senderName,
            receiverName = "${receiver.firstName} ${receiver.lastName}",
            orderId = request.orderId
        )
        
        val message = Message(
            conversationId = conversation.id!!,
            senderId = senderId,
            senderName = senderName,
            receiverId = request.receiverId,
            content = request.content
        )
        
        val savedMessage = messageRepository.save(message)
        
        // Update conversation with last message
        val updatedConversation = conversation.copy(
            lastMessage = request.content,
            lastMessageAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        conversationRepository.save(updatedConversation)
        
        return savedMessage
    }
    
    private fun findOrCreateConversation(
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        orderId: String?
    ): Conversation {
        // Find existing conversation between these two users (ignore orderId)
        // One buyer and one seller should only have ONE conversation
        val existingConversations = conversationRepository.findByParticipantsContaining(senderId)
        val existingConversation = existingConversations.find { conv ->
            conv.participants.contains(receiverId)
        }
        
        if (existingConversation != null) {
            return existingConversation
        }
        
        // Create new conversation (orderId is stored for reference but not used for lookup)
        val conversation = Conversation(
            participants = listOf(senderId, receiverId),
            participantNames = mapOf(senderId to senderName, receiverId to receiverName),
            orderId = orderId
        )
        
        return conversationRepository.save(conversation)
    }
    
    fun getConversations(userId: String): List<ConversationResponse> {
        val conversations = conversationRepository.findByParticipantsContaining(userId)
        return conversations.map { conv ->
            val unreadCount = messageRepository.countByConversationIdAndReceiverIdAndIsReadFalse(
                conv.id!!, userId
            )
            conv.toResponse(unreadCount.toInt())
        }.sortedByDescending { it.lastMessageAt }
    }
    
    fun getConversationMessages(conversationId: String, userId: String): ConversationWithMessagesResponse {
        val conversation = conversationRepository.findById(conversationId)
            .orElseThrow { IllegalArgumentException("Conversation not found") }
        
        // Verify user is a participant
        if (!conversation.participants.contains(userId)) {
            throw IllegalArgumentException("Access denied")
        }
        
        val messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
        
        // Mark messages as read
        val unreadMessages = messages.filter { it.receiverId == userId && !it.isRead }
        unreadMessages.forEach { msg ->
            messageRepository.save(msg.copy(isRead = true))
        }
        
        val unreadCount = messageRepository.countByConversationIdAndReceiverIdAndIsReadFalse(
            conversationId, userId
        )
        
        return ConversationWithMessagesResponse(
            conversation = conversation.toResponse(unreadCount.toInt()),
            messages = messages.map { it.toResponse() }
        )
    }
    
    fun getUnreadCount(userId: String): Int {
        return messageRepository.findByReceiverIdAndIsReadFalse(userId).size
    }
    
    fun Message.toResponse() = MessageResponse(
        id = id!!,
        conversationId = conversationId,
        senderId = senderId,
        senderName = senderName,
        receiverId = receiverId,
        content = content,
        isRead = isRead,
        createdAt = createdAt
    )
    
    fun Conversation.toResponse(unreadCount: Int) = ConversationResponse(
        id = id!!,
        participants = participants,
        participantNames = participantNames,
        orderId = orderId,
        lastMessage = lastMessage,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
