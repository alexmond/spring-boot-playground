package org.alexmond.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatResponse {

    private String userMessage;
    private String aiResponse;
    private LocalDateTime timestamp;
    private String sessionId;
}
