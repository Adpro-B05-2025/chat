package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatEditRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.security.UserPrincipal;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatWebSocketController {

    private final IChatMessageService msgSvc;
    private final ChatRoomService roomSvc;
    private final SimpMessagingTemplate ws;

    @Autowired
    public ChatWebSocketController(IChatMessageService msgSvc,
                                   ChatRoomService roomSvc,
                                   SimpMessagingTemplate ws) {
        this.msgSvc = msgSvc;
        this.roomSvc = roomSvc;
        this.ws = ws;
    }

    /**
     * 1) Pacilian inisiasi room dengan doctorId
     *    Manual broadcast roomId ke topic untuk memastikan client menerima
     */
    @MessageMapping("/chat.init.{doctorId}")
    public void initRoom(@DestinationVariable Long doctorId,
                         Principal principal) {
        Long pacId = ((UserPrincipal) principal).getId();
        ChatRoom room = roomSvc.createIfNotExists(pacId, doctorId);
        // kirim ID room langsung ke semua subscriber /topic/chat.init.{doctorId}
        ws.convertAndSend(
                "/topic/chat.init." + doctorId,
                room.getId()
        );
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

    /**
     * 5) Ambil history pesan
     */
    @MessageMapping("/chat.history.{roomId}")
    public List<ChatMessage> history(@DestinationVariable Long roomId, Principal p) {
        return msgSvc.getMessagesByRoom(roomId);
    }
}