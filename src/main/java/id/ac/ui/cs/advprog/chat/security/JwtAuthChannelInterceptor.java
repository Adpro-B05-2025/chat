package id.ac.ui.cs.advprog.chat.security;

import id.ac.ui.cs.advprog.chat.dto.TokenValidationResponse;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthChannelInterceptor implements ChannelInterceptor {

    private final ChatRoomService roomSvc;
    private final RestTemplate restTemplate;
    // URL ke auth‐profile untuk validasi token
    private final String authValidateUrl = "http://localhost:8081/api/auth/validate";

    @Autowired
    public JwtAuthChannelInterceptor(ChatRoomService roomSvc,
                                     RestTemplate restTemplate) {
        this.roomSvc = roomSvc;
        this.restTemplate = restTemplate;
    }

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel channel) {
        StompHeaderAccessor sh = StompHeaderAccessor.wrap(msg);

        // 1) Handle STOMP CONNECT: baca header Authorization, validasi, set UserPrincipal
        if (StompCommand.CONNECT.equals(sh.getCommand())) {
            List<String> authHeaders = sh.getNativeHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty() && authHeaders.get(0).startsWith("Bearer ")) {
                String token = authHeaders.get(0).substring(7);
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", "Bearer " + token);
                    HttpEntity<Void> request = new HttpEntity<>(headers);
                    ResponseEntity<TokenValidationResponse> resp =
                            restTemplate.exchange(authValidateUrl, HttpMethod.POST, request, TokenValidationResponse.class);
                    TokenValidationResponse tvr = resp.getBody();
                    if (resp.getStatusCode().is2xxSuccessful() && tvr != null && tvr.isValid()) {
                        var authorities = tvr.getRoles().stream()
                                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                                .collect(Collectors.toList());
                        UserPrincipal principal =
                                new UserPrincipal(tvr.getUserId(), tvr.getUsername(), authorities);
                        sh.setUser(principal);
                    }
                } catch (Exception ignored) {
                    // kalau gagal, biarkan sh.getUser() tetap null → nanti SEND akan error
                }
            }
            return sh.getMessage();
        }

        // 2) Handle SEND dst: sekarang sh.getUser() sudah ter‐set jika CONNECT sukses
        Principal user = sh.getUser();
        if (StompCommand.SEND.equals(sh.getCommand())) {
            String dest = sh.getDestination();
            if (dest != null) {
                // 2.a) Init room
                if (dest.startsWith("/app/chat.init.")) {
                    Long doctorId = Long.valueOf(dest.substring(dest.lastIndexOf('.') + 1));
                    if (!(user instanceof UserPrincipal)) {
                        throw new AccessDeniedException("Unauthenticated");
                    }
                    Long uid = ((UserPrincipal) user).getId();
                    if (uid.equals(doctorId)) {
                        throw new AccessDeniedException("Doctor cannot open a new chat");
                    }
                }
                // 2.b) Other room actions
                else if (dest.startsWith("/app/chat.send.")
                        || dest.startsWith("/app/chat.edit.")
                        || dest.startsWith("/app/chat.delete.")
                        || dest.startsWith("/app/chat.history.")) {
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