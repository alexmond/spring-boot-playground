package org.alexmond.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000)
    private String userMessage;

    @Column(nullable = false, length = 4000)
    private String aiResponse;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "session_id")
    private String sessionId;

    public ChatMessage(String userMessage, String aiResponse, String sessionId) {
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }
}
