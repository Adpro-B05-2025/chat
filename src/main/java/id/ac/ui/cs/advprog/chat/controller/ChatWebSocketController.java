// src/main/java/id/ac/ui/cs/advprog/chat/controller/ChatWebSocketController.java
package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatEditRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.security.UserPrincipal;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final IChatMessageService msgSvc;
    private final ChatRoomService roomSvc;

    public ChatWebSocketController(IChatMessageService msgSvc, ChatRoomService roomSvc) {
        this.msgSvc = msgSvc;
        this.roomSvc = roomSvc;
    }

    /**
     * 1) Pacilian inisiasi room dengan doctorId
     */
    @MessageMapping("/chat.init.{doctorId}")
    public Long initRoom(@DestinationVariable Long doctorId, Principal principal) {
        Long pacId = ((UserPrincipal) principal).getId();
        ChatRoom room = roomSvc.createIfNotExists(pacId, doctorId);
        return room.getId();
    }

    /**
     * 2) Kirim pesan ke room tertentu
     */
    @MessageMapping("/chat.send.{roomId}")
    public ChatMessage send(@DestinationVariable Long roomId,
                            ChatMessage message,
                            Principal principal) {
        message.setRoomId(roomId);
        return msgSvc.sendMessage(message);
    }

    /**
     * 3) Edit pesan (soft‐update)
     */
    @MessageMapping("/chat.edit.{roomId}")
    public ChatMessage edit(@DestinationVariable Long roomId,
                            ChatEditRequest req) {
        req.setRoomId(roomId);
        return msgSvc.editMessage(req.getId(), req.getNewContent())
                .orElseThrow(() ->
                        new RuntimeException("Message not found with id: " + req.getId()));
    }

    /**
     * 4) Delete pesan (soft‐delete)
     */
    @MessageMapping("/chat.delete.{roomId}")
    public ChatMessage delete(@DestinationVariable Long roomId,
                              ChatDeleteRequest req) {
        req.setRoomId(roomId);
        return msgSvc.deleteMessage(req.getId())
                .orElseThrow(() ->
                        new RuntimeException("Message not found with id: " + req.getId()));
    }
}
