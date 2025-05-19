package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageEditedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageDeletedEvent;
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
        ChatMessage msg = ev.getMessage();
        Long roomId = msg.getRoomId();
        ws.convertAndSend("/topic/chat." + roomId + ".messages", msg);
    }

    @EventListener
    public void onEdited(ChatMessageEditedEvent ev) {
        ChatMessage msg = ev.getMessage();
        Long roomId = msg.getRoomId();
        ws.convertAndSend("/topic/chat." + roomId + ".updates", msg);
    }

    @EventListener
    public void onDeleted(ChatMessageDeletedEvent ev) {
        ChatMessage msg = ev.getMessage();
        Long roomId = msg.getRoomId();
        ws.convertAndSend("/topic/chat." + roomId + ".updates", msg);
    }
}
