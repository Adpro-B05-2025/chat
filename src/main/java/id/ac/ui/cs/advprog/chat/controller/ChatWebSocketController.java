package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatEditRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ChatWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketController.class);

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
        logger.info("ChatWebSocketController initialized");
    }

    @MessageMapping("/chat.init.{targetUserId}")
    public void initRoom(@DestinationVariable Long targetUserId,
                         SimpMessageHeaderAccessor headerAccessor) {
        logger.info("initRoom called - targetUserId={}", targetUserId);
        logger.debug("Session attributes on init: {}", headerAccessor.getSessionAttributes());
        try {
            Long currentUserId = getUserId(headerAccessor);
            String authToken = getAuthToken(headerAccessor);

            logger.info("Current User ID for initRoom: {}", currentUserId);
            logger.debug("Auth token present: {}", authToken != null);

            if (authToken != null) {
                ChatRoomService.setCurrentAuthToken(authToken);
            }

            try {
                ChatRoom room = roomSvc.createIfNotExists(currentUserId, targetUserId);
                logger.info("Room created/found with ID: {}", room.getId());

                ws.convertAndSendToUser(
                        currentUserId.toString(),
                        "/topic/chat.init",
                        room.getId()
                );
                ws.convertAndSend(
                        "/topic/chat.init." + targetUserId,
                        room.getId()
                );

                logger.info("initRoom completed successfully for roomId={}", room.getId());
            } finally {
                ChatRoomService.clearCurrentAuthToken();
                logger.debug("Cleared thread-local auth token");
            }
        } catch (Exception e) {
            logger.error("Error in initRoom for targetUserId={}: {}", targetUserId, e.getMessage(), e);
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
        logger.info("send called - roomId={}", roomId);
        logger.debug("Payload before send: {}", message);
        try {
            Long senderId = getUserId(headerAccessor);
            message.setRoomId(roomId);
            message.setSenderId(senderId);

            logger.debug("Message details - sender={}, receiver={}, content={}",
                    senderId, message.getReceiverId(), message.getContent());

            ChatMessage saved = msgSvc.sendMessage(message);
            logger.info("Message sent successfully - messageId={}", saved.getId());
            logger.debug("send completed for messageId={}", saved.getId());
        } catch (Exception e) {
            logger.error("Error in send for roomId={}: {}", roomId, e.getMessage(), e);
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
        logger.info("edit called - roomId={}, messageId={}", roomId, req.getId());
        logger.debug("edit request payload: {}", req);
        try {
            Long userId = getUserId(headerAccessor);
            logger.debug("User {} editing message {}", userId, req.getId());

            req.setRoomId(roomId);
            ChatMessage updated = msgSvc.editMessage(req.getId(), req.getNewContent())
                    .orElseThrow(() -> new RuntimeException("Message not found with id: " + req.getId()));

            logger.info("Message edited successfully - messageId={}", updated.getId());
            logger.debug("edit completed for messageId={}", updated.getId());
        } catch (Exception e) {
            logger.error("Error in edit for roomId={}, messageId={}: {}", roomId, req.getId(), e.getMessage(), e);
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
        logger.info("delete called - roomId={}, messageId={}", roomId, req.getId());
        logger.debug("delete request payload: {}", req);
        try {
            Long userId = getUserId(headerAccessor);
            logger.debug("User {} deleting message {}", userId, req.getId());

            req.setRoomId(roomId);
            ChatMessage deleted = msgSvc.deleteMessage(req.getId())
                    .orElseThrow(() -> new RuntimeException("Message not found with id: " + req.getId()));

            logger.info("Message deleted successfully - messageId={}", deleted.getId());
            logger.debug("delete completed for messageId={}", deleted.getId());
        } catch (Exception e) {
            logger.error("Error in delete for roomId={}, messageId={}: {}", roomId, req.getId(), e.getMessage(), e);
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to delete message: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.history.{roomId}")
    public void history(@DestinationVariable Long roomId,
                        SimpMessageHeaderAccessor headerAccessor) {
        logger.info("history called - roomId={}", roomId);
        try {
            Long userId = getUserId(headerAccessor);
            logger.debug("User {} requesting history for room {}", userId, roomId);

            List<ChatMessage> messages = msgSvc.getMessagesByRoom(roomId);
            logger.info("Retrieved {} messages for room {}", messages.size(), roomId);

            if (!messages.isEmpty()) {
                for (ChatMessage message : messages) {
                    ws.convertAndSend("/topic/chat." + roomId + ".messages", message);
                }
            }

            ws.convertAndSend("/topic/chat." + roomId + ".history.complete",
                    "History loaded: " + messages.size() + " messages");
            logger.debug("history completed - sent {} messages for roomId={}", messages.size(), roomId);
        } catch (Exception e) {
            logger.error("Error in history for roomId={}: {}", roomId, e.getMessage(), e);
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to fetch history: " + e.getMessage()
            );
        }
    }

    // Helper method to extract user ID from session attributes
    private Long getUserId(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            logger.error("Header accessor is null");
            throw new RuntimeException("Cannot access session information");
        }

        // First try to get from session attributes
        Long userId = (Long) headerAccessor.getSessionAttributes().get(USER_ID_SESSION_ATTR);
        logger.debug("Retrieved userId from session attributes: {}", userId);

        if (userId != null) {
            return userId;
        }

        // If not found in session attributes, try to get from message attributes
        userId = (Long) headerAccessor.getHeader(USER_ID_SESSION_ATTR);
        logger.debug("Retrieved userId from message headers: {}", userId);

        if (userId != null) {
            return userId;
        }

        // Last resort: try to get session ID and log for debugging
        String sessionId = headerAccessor.getSessionId();
        logger.error("User ID not found. Session ID: {}, Session attributes: {}, Message headers: {}",
                sessionId,
                headerAccessor.getSessionAttributes(),
                headerAccessor.toMap());

        throw new RuntimeException("User not authenticated - no user ID found in session or headers");
    }

    // Helper method to extract auth token from session attributes
    private String getAuthToken(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            return null;
        }

        // Try to get from session attributes
        String authToken = (String) headerAccessor.getSessionAttributes().get(AUTH_TOKEN_SESSION_ATTR);
        logger.debug("Retrieved auth token from session attributes: {}", authToken != null ? "present" : "null");

        if (authToken != null) {
            return authToken;
        }

        // Try to get from message headers
        authToken = (String) headerAccessor.getHeader(AUTH_TOKEN_SESSION_ATTR);
        logger.debug("Retrieved auth token from message headers: {}", authToken != null ? "present" : "null");

        return authToken;
    }
}
