// src/main/java/id/ac/ui/cs/advprog/chat/listener/ChatNotificationListener.java
package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.*;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatNotificationListener {
    private final SimpMessagingTemplate ws;

    public ChatNotificationListener(SimpMessagingTemplate ws) {
        this.ws = ws;
    }

    @EventListener
    public void onSent(ChatMessageSentEvent ev) {
        ws.convertAndSend("/topic/messages", ev.getMessage());
    }

    @EventListener
    public void onEdited(ChatMessageEditedEvent ev) {
        ws.convertAndSend("/topic/updates", ev.getMessage());
    }

    @EventListener
    public void onDeleted(ChatMessageDeletedEvent ev) {
        ws.convertAndSend("/topic/updates", ev.getMessage());
    }
}