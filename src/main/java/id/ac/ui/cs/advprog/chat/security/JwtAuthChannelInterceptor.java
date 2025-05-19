// src/main/java/id/ac/ui/cs/advprog/chat/security/JwtAuthChannelInterceptor.java
package id.ac.ui.cs.advprog.chat.security;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private ChatRoomService roomSvc;

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel channel) {
        StompHeaderAccessor sh = StompHeaderAccessor.wrap(msg);
        Principal user = sh.getUser(); // set by your JwtAuthFilter

        // only enforce on STOMP SEND operations
        if (StompCommand.SEND.equals(sh.getCommand())) {
            String dest = sh.getDestination();
            // expect destinations like "/app/chat.send.{roomId}"
            if (dest != null && dest.startsWith("/app/chat.send.")) {
                Long roomId = Long.valueOf(dest.substring(dest.lastIndexOf('.') + 1));
                ChatRoom room = roomSvc
                        .find(roomId)
                        .orElseThrow(() -> new AccessDeniedException("Chat room not found"));

                Long uid = ((UserPrincipal) user).getId();
                boolean isPac = uid.equals(room.getPacilianId());
                boolean isDoc = uid.equals(room.getDoctorId());
                // only pacilian or doctor members of this room may send
                if (!isPac && !isDoc) {
                    throw new AccessDeniedException("You are not a member of this chat room");
                }
            }
        }

        return msg;
    }
}