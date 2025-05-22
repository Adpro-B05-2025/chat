package id.ac.ui.cs.advprog.chat.security;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthChannelInterceptor.class);

    private final ChatRoomService roomSvc;
    private final JwtUtils jwtUtils;

    // Session attribute key for storing user info
    private static final String USER_ID_SESSION_ATTR = "USER_ID";
    private static final String USERNAME_SESSION_ATTR = "USERNAME";
    private static final String AUTH_TOKEN_SESSION_ATTR = "AUTH_TOKEN";

    @Autowired
    public JwtAuthChannelInterceptor(ChatRoomService roomSvc, JwtUtils jwtUtils) {
        this.roomSvc = roomSvc;
        this.jwtUtils = jwtUtils;
    }

    // Simple Principal wrapper
    private static class SimplePrincipal implements Principal {
        private final String name;
        private final Long userId;

        public SimplePrincipal(String name, Long userId) {
            this.name = name;
            this.userId = userId;
        }

        @Override
        public String getName() {
            return name;
        }

        public Long getUserId() {
            return userId;
        }

        @Override
        public String toString() {
            return "SimplePrincipal{name='" + name + "', userId=" + userId + '}';
        }
    }

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel channel) {
        StompHeaderAccessor sh = StompHeaderAccessor.wrap(msg);

        logger.debug("Processing STOMP command: {} on thread: {}", sh.getCommand(), Thread.currentThread().getName());
        logger.debug("Destination: {}", sh.getDestination());
        logger.debug("Session ID: {}", sh.getSessionId());

        try {
            // 1) Handle STOMP CONNECT: validate JWT token and store user info in session
            if (StompCommand.CONNECT.equals(sh.getCommand())) {
                logger.info("Processing STOMP CONNECT");

                List<String> authHeaders = sh.getNativeHeader("Authorization");
                logger.debug("Authorization headers: {}", authHeaders);

                if (authHeaders != null && !authHeaders.isEmpty() && authHeaders.get(0).startsWith("Bearer ")) {
                    String token = authHeaders.get(0).substring(7);
                    logger.debug("Extracted token: {}...", token.substring(0, Math.min(token.length(), 20)));

                    try {
                        if (jwtUtils.validateJwtToken(token)) {
                            logger.info("JWT token is valid");

                            String userId = jwtUtils.getUserIdFromJwtToken(token);
                            String username = jwtUtils.getUsernameFromToken(token);

                            logger.debug("Extracted userId: {}, username: {}", userId, username);

                            if (userId != null) {
                                // Store user info in session attributes
                                sh.getSessionAttributes().put(USER_ID_SESSION_ATTR, Long.parseLong(userId));
                                sh.getSessionAttributes().put(USERNAME_SESSION_ATTR, username != null ? username : userId);
                                sh.getSessionAttributes().put(AUTH_TOKEN_SESSION_ATTR, authHeaders.get(0)); // Store full Bearer token

                                // Also set as principal
                                SimplePrincipal principal = new SimplePrincipal(
                                        username != null ? username : userId,
                                        Long.parseLong(userId)
                                );
                                sh.setUser(principal);

                                logger.info("Successfully set user principal and session attributes for userId: {}", userId);
                            } else {
                                logger.warn("Missing userId in token");
                            }
                        } else {
                            logger.warn("JWT token validation failed");
                        }
                    } catch (Exception e) {
                        logger.error("Error processing JWT token: ", e);
                    }
                } else {
                    logger.warn("No valid Authorization header found");
                }

                return msg; // Always return the message for CONNECT
            }

            // 2) For other commands, get user info from session attributes
            Principal user = sh.getUser();
            logger.debug("Current user principal: {}", user);

            // If user is null, try to get from session attributes
            if (user == null) {
                logger.debug("User principal is null, attempting to get from session attributes");

                Long userId = (Long) sh.getSessionAttributes().get(USER_ID_SESSION_ATTR);
                String username = (String) sh.getSessionAttributes().get(USERNAME_SESSION_ATTR);
                String authToken = (String) sh.getSessionAttributes().get(AUTH_TOKEN_SESSION_ATTR);

                logger.debug("Session attributes - userId: {}, username: {}, authToken: {}",
                        userId, username, authToken != null ? "present" : "null");

                if (userId != null) {
                    SimplePrincipal principal = new SimplePrincipal(username, userId);
                    sh.setUser(principal);
                    user = principal;

                    // Also store in message headers for controller access
                    sh.setHeader(USER_ID_SESSION_ATTR, userId);
                    sh.setHeader(USERNAME_SESSION_ATTR, username);
                    sh.setHeader(AUTH_TOKEN_SESSION_ATTR, authToken);

                    logger.info("Successfully restored user principal from session for userId: {}", userId);
                } else {
                    logger.warn("No user information found in session attributes");
                }
            } else {
                // If user exists, ensure headers are set for controller
                if (user instanceof SimplePrincipal) {
                    SimplePrincipal simplePrincipal = (SimplePrincipal) user;
                    sh.setHeader(USER_ID_SESSION_ATTR, simplePrincipal.getUserId());
                    sh.setHeader(USERNAME_SESSION_ATTR, simplePrincipal.getName());
                    // Try to get auth token from session for existing user
                    String authToken = (String) sh.getSessionAttributes().get(AUTH_TOKEN_SESSION_ATTR);
                    sh.setHeader(AUTH_TOKEN_SESSION_ATTR, authToken);
                    logger.debug("Set headers for existing principal: {}", simplePrincipal.getUserId());
                }
            }

            // 3) Handle SEND commands: check permissions
            if (StompCommand.SEND.equals(sh.getCommand())) {
                String dest = sh.getDestination();
                logger.info("Processing STOMP SEND to destination: {}", dest);

                if (dest != null) {
                    // 3.a) Init room - any authenticated user can initiate
                    if (dest.startsWith("/app/chat.init.")) {
                        logger.info("Processing chat init request");

                        try {
                            Long targetUserId = Long.valueOf(dest.substring(dest.lastIndexOf('.') + 1));
                            logger.debug("Target User ID: {}", targetUserId);

                            if (!(user instanceof SimplePrincipal)) {
                                logger.error("User not authenticated for chat init - user type: {}",
                                        user != null ? user.getClass().getSimpleName() : "null");
                                throw new AccessDeniedException("Unauthenticated");
                            }

                            Long currentUserId = ((SimplePrincipal) user).getUserId();
                            logger.debug("Current User ID: {}", currentUserId);

                            if (currentUserId.equals(targetUserId)) {
                                logger.error("User cannot initiate chat with themselves");
                                throw new AccessDeniedException("Cannot start chat with yourself");
                            }

                            logger.info("Chat init request authorized for user {} with target user {}",
                                    currentUserId, targetUserId);

                        } catch (NumberFormatException e) {
                            logger.error("Invalid target user ID in destination: {}", dest);
                            throw new AccessDeniedException("Invalid target user ID");
                        }
                    }
                    // 3.b) Other room actions - check membership
                    else if (dest.startsWith("/app/chat.send.")
                            || dest.startsWith("/app/chat.edit.")
                            || dest.startsWith("/app/chat.delete.")
                            || dest.startsWith("/app/chat.history.")) {

                        logger.info("Processing room action request");

                        if (!(user instanceof SimplePrincipal)) {
                            logger.error("User not authenticated for room action - user type: {}",
                                    user != null ? user.getClass().getSimpleName() : "null");
                            throw new AccessDeniedException("Unauthenticated");
                        }

                        try {
                            Long roomId = Long.valueOf(dest.substring(dest.lastIndexOf('.') + 1));
                            logger.debug("Room ID: {}", roomId);

                            ChatRoom room = roomSvc.find(roomId)
                                    .orElseThrow(() -> {
                                        logger.error("Chat room not found: {}", roomId);
                                        return new AccessDeniedException("Chat room not found");
                                    });

                            Long uid = ((SimplePrincipal) user).getUserId();
                            boolean isPacillian = uid.equals(room.getPacilianId());
                            boolean isCaregiver = uid.equals(room.getDoctorId());

                            logger.debug("User {} checking membership in room {} (pacillian: {}, caregiver: {})",
                                    uid, roomId, room.getPacilianId(), room.getDoctorId());

                            if (!isPacillian && !isCaregiver) {
                                logger.error("User {} not authorized for room {}", uid, roomId);
                                throw new AccessDeniedException("You are not a member of this chat room");
                            }

                            logger.info("Room action authorized for user {} in room {}", uid, roomId);

                        } catch (NumberFormatException e) {
                            logger.error("Invalid room ID in destination: {}", dest);
                            throw new AccessDeniedException("Invalid room ID");
                        }
                    }
                }
            }

            logger.debug("Message processing completed successfully");
            return msg;

        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in channel interceptor: ", e);
            throw new AccessDeniedException("Authentication error: " + e.getMessage());
        }
    }
}