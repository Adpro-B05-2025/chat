package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatEditRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatWebSocketController {

    private final IChatMessageService msgSvc;
    private final ChatRoomService roomSvc;
    private final SimpMessagingTemplate ws;

    private static final String USER_ID_SESSION_ATTR = "USER_ID";
    private static final String USERNAME_SESSION_ATTR = "USERNAME";
    private static final String AUTH_TOKEN_SESSION_ATTR = "AUTH_TOKEN";

    @Autowired
    public ChatWebSocketController(IChatMessageService msgSvc,
                                   ChatRoomService roomSvc,
                                   SimpMessagingTemplate ws) {
        this.msgSvc = msgSvc;
        this.roomSvc = roomSvc;
        this.ws = ws;
    }

    @MessageMapping("/chat.init.{targetUserId}")
    public void initRoom(@DestinationVariable Long targetUserId,
                         SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long currentUserId = getUserId(headerAccessor);
            String authToken = getAuthToken(headerAccessor);

            if (authToken != null) {
                ChatRoomService.setCurrentAuthToken(authToken);
            }

            try {
                ChatRoom room = roomSvc.createIfNotExists(currentUserId, targetUserId);

                ws.convertAndSendToUser(
                        currentUserId.toString(),
                        "/topic/chat.init",
                        room.getId()
                );
                ws.convertAndSend(
                        "/topic/chat.init." + targetUserId,
                        room.getId()
                );
            } finally {
                ChatRoomService.clearCurrentAuthToken();
            }
        } catch (Exception e) {
            ws.convertAndSend(
                    "/topic/chat.init." + targetUserId + ".error",
                    "Failed to initialize room: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.send.{roomId}")
    public void send(@DestinationVariable Long roomId,
                     ChatMessage message,
                     SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long senderId = getUserId(headerAccessor);
            message.setRoomId(roomId);
            message.setSenderId(senderId);

            msgSvc.sendMessage(message);
        } catch (Exception e) {
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to send message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.edit.{roomId}")
    public void edit(@DestinationVariable Long roomId,
                     ChatEditRequest req,
                     SimpMessageHeaderAccessor headerAccessor) {
        try {
            getUserId(headerAccessor);

            req.setRoomId(roomId);
            msgSvc.editMessage(req.getId(), req.getNewContent())
                    .orElseThrow(() -> new RuntimeException("Message not found with id: " + req.getId()));
        } catch (Exception e) {
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to edit message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.delete.{roomId}")
    public void delete(@DestinationVariable Long roomId,
                       ChatDeleteRequest req,
                       SimpMessageHeaderAccessor headerAccessor) {
        try {
            getUserId(headerAccessor);

            req.setRoomId(roomId);
            msgSvc.deleteMessage(req.getId())
                    .orElseThrow(() -> new RuntimeException("Message not found with id: " + req.getId()));
        } catch (Exception e) {
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to delete message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.history.{roomId}")
    public void history(@DestinationVariable Long roomId,
                        SimpMessageHeaderAccessor headerAccessor) {
        try {
            getUserId(headerAccessor);
            List<ChatMessage> messages = msgSvc.getMessagesByRoom(roomId);

            if (!messages.isEmpty()) {
                for (ChatMessage message : messages) {
                    ws.convertAndSend("/topic/chat." + roomId + ".messages", message);
                }
            }

            ws.convertAndSend("/topic/chat." + roomId + ".history.complete",
                    "History loaded: " + messages.size() + " messages");
        } catch (Exception e) {
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to fetch history: " + e.getMessage()
            );
        }
    }

    private Long getUserId(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            throw new RuntimeException("Cannot access session information");
        }

        Long userId = (Long) headerAccessor.getSessionAttributes().get(USER_ID_SESSION_ATTR);
        if (userId != null) {
            return userId;
        }

        userId = (Long) headerAccessor.getHeader(USER_ID_SESSION_ATTR);
        if (userId != null) {
            return userId;
        }

        throw new RuntimeException("User not authenticated - no user ID found in session or headers");
    }

    private String getAuthToken(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            return null;
        }

        String authToken = (String) headerAccessor.getSessionAttributes().get(AUTH_TOKEN_SESSION_ATTR);
        if (authToken != null) {
            return authToken;
        }

        return (String) headerAccessor.getHeader(AUTH_TOKEN_SESSION_ATTR);
    }
}
