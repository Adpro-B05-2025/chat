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

    // Session attribute keys - must match interceptor
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

    /**
     * Initialize chat room between current user and target user.
     * This works for both patients initiating with caregivers and caregivers initiating with patients.
     *
     * @param targetUserId The ID of the user to start a chat with
     * @param headerAccessor Session information
     */
    @MessageMapping("/chat.init.{targetUserId}")
    public void initRoom(@DestinationVariable Long targetUserId,
                         SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Initializing room - Target User ID: {}", targetUserId);

        try {
            Long currentUserId = getUserId(headerAccessor);
            String authToken = getAuthToken(headerAccessor);

            logger.info("Current User ID: {}", currentUserId);
            logger.debug("Auth token present: {}", authToken != null);

            // Set the auth token in thread-local for the service to use
            if (authToken != null) {
                ChatRoomService.setCurrentAuthToken(authToken);
            }

            try {
                // Use the updated createIfNotExists method that handles role assignment automatically
                ChatRoom room = roomSvc.createIfNotExists(currentUserId, targetUserId);
                logger.info("Room created/found with ID: {}", room.getId());

                // Send room ID back to the initiating user
                // Use current user ID as the topic identifier since they're the ones waiting for the response
                ws.convertAndSendToUser(
                        currentUserId.toString(),
                        "/topic/chat.init",
                        room.getId()
                );

                // Also send to the general topic for backwards compatibility
                ws.convertAndSend(
                        "/topic/chat.init." + targetUserId,
                        room.getId()
                );

                logger.info("Room initialization successful - sent room ID {} to both users", room.getId());
            } finally {
                // Always clear the thread-local token
                ChatRoomService.clearCurrentAuthToken();
            }

        } catch (Exception e) {
            logger.error("Error initializing room: ", e);
            // Send error to client
            ws.convertAndSend(
                    "/topic/chat.init." + targetUserId + ".error",
                    "Failed to initialize room: " + e.getMessage()
            );
        }
    }

    /**
     * 2) Send message to specific room
     */
    @MessageMapping("/chat.send.{roomId}")
    public void send(@DestinationVariable Long roomId,
                     ChatMessage message,
                     SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Sending message to room: {}", roomId);

        try {
            Long senderId = getUserId(headerAccessor);

            message.setRoomId(roomId);
            message.setSenderId(senderId);

            logger.debug("Message details - Sender: {}, Receiver: {}, Content: {}",
                    senderId, message.getReceiverId(), message.getContent());

            ChatMessage saved = msgSvc.sendMessage(message);
            logger.info("Message sent successfully with ID: {}", saved.getId());

        } catch (Exception e) {
            logger.error("Error sending message: ", e);
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to send message: " + e.getMessage()
            );
        }
    }

    /**
     * 3) Edit message (soft-update)
     */
    @MessageMapping("/chat.edit.{roomId}")
    public void edit(@DestinationVariable Long roomId,
                     ChatEditRequest req,
                     SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Editing message {} in room: {}", req.getId(), roomId);

        try {
            Long userId = getUserId(headerAccessor);
            logger.debug("User {} editing message {}", userId, req.getId());

            req.setRoomId(roomId);
            ChatMessage updated = msgSvc.editMessage(req.getId(), req.getNewContent())
                    .orElseThrow(() -> {
                        logger.error("Message not found for edit: {}", req.getId());
                        return new RuntimeException("Message not found with id: " + req.getId());
                    });

            logger.info("Message edited successfully");

        } catch (Exception e) {
            logger.error("Error editing message: ", e);
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to edit message: " + e.getMessage()
            );
        }
    }

    /**
     * 4) Delete message (soft-delete)
     */
    @MessageMapping("/chat.delete.{roomId}")
    public void delete(@DestinationVariable Long roomId,
                       ChatDeleteRequest req,
                       SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Deleting message {} in room: {}", req.getId(), roomId);

        try {
            Long userId = getUserId(headerAccessor);
            logger.debug("User {} deleting message {}", userId, req.getId());

            req.setRoomId(roomId);
            ChatMessage deleted = msgSvc.deleteMessage(req.getId())
                    .orElseThrow(() -> {
                        logger.error("Message not found for delete: {}", req.getId());
                        return new RuntimeException("Message not found with id: " + req.getId());
                    });

            logger.info("Message deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting message: ", e);
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to delete message: " + e.getMessage()
            );
        }
    }

    /**
     * 5) Get message history for room
     */
    @MessageMapping("/chat.history.{roomId}")
    public void history(@DestinationVariable Long roomId,
                        SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Fetching history for room: {}", roomId);

        try {
            Long userId = getUserId(headerAccessor);
            logger.debug("User {} requesting history for room {}", userId, roomId);

            List<ChatMessage> messages = msgSvc.getMessagesByRoom(roomId);
            logger.info("Retrieved {} messages for room {}", messages.size(), roomId);

            // Send history to the room's messages topic so frontend can receive it
            if (!messages.isEmpty()) {
                for (ChatMessage message : messages) {
                    // Send each message to the room's messages topic
                    ws.convertAndSend("/topic/chat." + roomId + ".messages", message);
                }
            }

            // Also send a completion signal
            ws.convertAndSend("/topic/chat." + roomId + ".history.complete",
                    "History loaded: " + messages.size() + " messages");

        } catch (Exception e) {
            logger.error("Error fetching history: ", e);
            ws.convertAndSend(
                    "/topic/chat." + roomId + ".error",
                    "Failed to fetch history: " + e.getMessage()
            );
        }
    }
}