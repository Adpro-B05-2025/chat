package com.adpro.chat.event;

import com.adpro.chat.model.ChatMessage;
import org.springframework.context.ApplicationEvent;

public class ChatMessageSentEvent extends ApplicationEvent {
    public ChatMessageSentEvent(ChatMessage message) {
        super(message);
    }

    public ChatMessage getMessage() {
        return (ChatMessage) getSource();
    }
}
