package com.adpro.chat.service;

import com.adpro.chat.model.ChatMessage;
import com.adpro.chat.repository.ChatMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {

    private final ChatMessageRepository repo;
    private final ApplicationEventPublisher publisher;

    public ChatMessageService(ChatMessageRepository repo, ApplicationEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    public ChatMessage sendMessage(ChatMessage message) {
        return null;
    }
}
