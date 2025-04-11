package com.adpro.chat.listener;

import com.adpro.chat.event.ChatMessageSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatAuditLogger {

    @EventListener
    public void onMessageSent(ChatMessageSentEvent event) {
        System.out.println("[AUDIT] Message sent: " + event.getMessage().getContent());
    }
}