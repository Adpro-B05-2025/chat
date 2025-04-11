package id.ac.ui.cs.advprog.chat.event;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.springframework.context.ApplicationEvent;

public class ChatMessageSentEvent extends ApplicationEvent {
    public ChatMessageSentEvent(ChatMessage message) {
        super(message);
    }

    public ChatMessage getMessage() {
        return (ChatMessage) getSource();
    }
}
