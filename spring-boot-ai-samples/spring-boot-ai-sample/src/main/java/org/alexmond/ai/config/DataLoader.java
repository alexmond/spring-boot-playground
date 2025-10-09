package org.alexmond.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alexmond.ai.entity.ChatMessage;
import org.alexmond.ai.repository.ChatMessageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    public void run(String... args) throws Exception {
        if (chatMessageRepository.count() == 0) {
            log.info("Loading sample data...");

            // Add some sample chat messages
            ChatMessage sample1 = new ChatMessage(
                    "Hello, how are you?",
                    "Hello! I'm doing great, thank you for asking. How can I help you today?",
                    "sample-session-1"
            );

            ChatMessage sample2 = new ChatMessage(
                    "What can you do?",
                    "I'm an AI assistant that can help you with various tasks like answering questions, providing information, helping with analysis, and having conversations. What would you like to know?",
                    "sample-session-2"
            );

            chatMessageRepository.save(sample1);
            chatMessageRepository.save(sample2);

            log.info("Sample data loaded successfully");
        }
    }
}
