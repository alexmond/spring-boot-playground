package org.alexmond.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    
    @NotBlank(message = "Message cannot be empty")
    private String message;
    
    private String sessionId;
}
