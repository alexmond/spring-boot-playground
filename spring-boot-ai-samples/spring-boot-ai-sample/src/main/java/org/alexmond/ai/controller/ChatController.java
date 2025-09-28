package org.alexmond.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.ai.dto.ChatRequest;
import org.alexmond.ai.dto.ChatResponse;
import org.alexmond.ai.service.AiChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class ChatController {

    private final AiChatService aiChatService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("chatRequest", new ChatRequest());
        return "chat";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request.getMessage());
            ChatResponse response = aiChatService.sendMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/chat/history/{sessionId}")
    @ResponseBody
    public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable String sessionId) {
        try {
            List<ChatResponse> history = aiChatService.getChatHistory(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error retrieving chat history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/chat/recent")
    @ResponseBody
    public ResponseEntity<List<ChatResponse>> getRecentChats() {
        try {
            List<ChatResponse> recentChats = aiChatService.getRecentChats();
            return ResponseEntity.ok(recentChats);
        } catch (Exception e) {
            log.error("Error retrieving recent chats", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
