package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatAuditLogger {

    @EventListener
    public void onMessageSent(ChatMessageSentEvent event) {
        System.out.println("[AUDIT] Message sent: " + event.getMessage().getContent());
    }
}