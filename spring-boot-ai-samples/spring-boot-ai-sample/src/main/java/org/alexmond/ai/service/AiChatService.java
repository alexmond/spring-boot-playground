package org.alexmond.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.ai.dto.ChatRequest;
import org.alexmond.ai.dto.ChatResponse;
import org.alexmond.ai.entity.ChatMessage;
import org.alexmond.ai.repository.ChatMessageRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;

    public ChatResponse sendMessage(ChatRequest request) {
        try {
            // Generate session ID if not provided
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            log.info("Processing chat message for session: {}", sessionId);

            // Get AI response
            String aiResponse = chatClient.prompt()
                    .user(request.getMessage())
                    .call()
                    .content();

            // Save conversation to database
            ChatMessage chatMessage = new ChatMessage(
                    request.getMessage(),
                    aiResponse,
                    sessionId
            );
            chatMessageRepository.save(chatMessage);

            log.info("Chat message saved successfully");

            return new ChatResponse(
                    request.getMessage(),
                    aiResponse,
                    chatMessage.getTimestamp(),
                    sessionId
            );

        } catch (Exception e) {
            log.error("Error processing chat message", e);
            throw new RuntimeException("Failed to process chat message", e);
        }
    }

    public List<ChatResponse> getChatHistory(String sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        return messages.stream()
                .map(msg -> new ChatResponse(
                        msg.getUserMessage(),
                        msg.getAiResponse(),
                        msg.getTimestamp(),
                        msg.getSessionId()
                ))
                .collect(Collectors.toList());
    }

    public List<ChatResponse> getRecentChats() {
        List<ChatMessage> messages = chatMessageRepository.findTop10ByOrderByTimestampDesc();
        return messages.stream()
                .map(msg -> new ChatResponse(
                        msg.getUserMessage(),
                        msg.getAiResponse(),
                        msg.getTimestamp(),
                        msg.getSessionId()
                ))
                .collect(Collectors.toList());
    }
}
