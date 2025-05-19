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

    private final ChatRoomService roomSvc;

    @Autowired
    public JwtAuthChannelInterceptor(ChatRoomService roomSvc) {
        this.roomSvc = roomSvc;
    }

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel channel) {
        StompHeaderAccessor sh = StompHeaderAccessor.wrap(msg);
        Principal user = sh.getUser();

        if (StompCommand.SEND.equals(sh.getCommand())) {
            String dest = sh.getDestination();
            if (dest != null) {
                // 1) NEW CHAT: only pacilian may init a new room
                if (dest.startsWith("/app/chat.init.")) {
                    Long doctorId = Long.valueOf(dest.substring(dest.lastIndexOf('.') + 1));
                    Long uid = ((UserPrincipal) user).getId();
                    if (uid.equals(doctorId)) {
                        throw new AccessDeniedException("Doctor cannot open a new chat");
                    }
                }
                // 2) ALL OTHER ROOM ACTIONS: send/edit/delete need membership
                else if (dest.startsWith("/app/chat.send.")
                        || dest.startsWith("/app/chat.edit.")
                        || dest.startsWith("/app/chat.delete.")
                        || dest.startsWith("/app/chat.history.")) {  // if you protect history too
                    Long roomId = Long.valueOf(dest.substring(dest.lastIndexOf('.') + 1));
                    ChatRoom room = roomSvc.find(roomId)
                            .orElseThrow(() -> new AccessDeniedException("Chat room not found"));

                    Long uid = ((UserPrincipal) user).getId();
                    boolean isPac = uid.equals(room.getPacilianId());
                    boolean isDoc = uid.equals(room.getDoctorId());
                    if (!isPac && !isDoc) {
                        throw new AccessDeniedException("You are not a member of this chat room");
                    }
                }
            }
        }

        return msg;
    }
}
