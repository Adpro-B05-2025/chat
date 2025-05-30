package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import id.ac.ui.cs.advprog.chat.service.AuthProfileService;
import id.ac.ui.cs.advprog.chat.dto.ContactResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatRestController {

    private static final Logger logger = LoggerFactory.getLogger(ChatRestController.class);

    private final ChatRoomService roomService;
    private final IChatMessageService messageService;
    private final AuthProfileService authProfileService;

    @Autowired
    public ChatRestController(ChatRoomService roomService,
                              IChatMessageService messageService,
                              AuthProfileService authProfileService) {
        this.roomService = roomService;
        this.messageService = messageService;
        this.authProfileService = authProfileService;
    }

    /**
     * Get list of contacts (people the user has chatted with)
     */
    @GetMapping("/contacts")
    public ResponseEntity<List<ContactResponse>> getContacts(@RequestParam Long userId) {
        logger.info("START getContacts for userId={}", userId);

        try {
            // 1. Ambil semua chat room yang melibatkan user ini
            List<ChatRoom> userRooms = roomService.findRoomsByUserId(userId);
            logger.debug("Found {} rooms for user {}", userRooms.size(), userId);

            List<ContactResponse> contacts = new ArrayList<>();

            // 2. Proses tiap room
            for (ChatRoom room : userRooms) {
                Long contactId = userId.equals(room.getPacilianId())
                        ? room.getDoctorId()
                        : room.getPacilianId();
                logger.debug("Room {}: contactId={}", room.getId(), contactId);

                // 2.a. Ambil nama dari AuthProfileService
                String contactName = authProfileService.getUserName(contactId);
                logger.debug("Contact name for {} => {}", contactId, contactName);

                // 2.b. Ambil pesan terakhir untuk preview
                List<ChatMessage> messages = messageService.getMessagesByRoom(room.getId());
                logger.debug("Room {}: {} messages loaded", room.getId(), messages.size());

                String lastMessage = "";
                if (!messages.isEmpty()) {
                    ChatMessage lastMsg = messages.get(messages.size() - 1);
                    lastMessage = "deleted".equalsIgnoreCase(lastMsg.getStatus())
                            ? "Message deleted"
                            : lastMsg.getContent();
                }

                // 2.c. Bangun response
                ContactResponse contact = new ContactResponse();
                contact.setContactId(contactId);
                contact.setContactName(contactName);
                contact.setRoomId(room.getId());
                contact.setLastMessage(lastMessage);
                contact.setLastMessageTime(
                    messages.isEmpty() ? null : messages.get(messages.size() - 1).getTimestamp()
                );

                contacts.add(contact);
                logger.debug("Added ContactResponse: {}", contact);
            }

            // 3. Urutkan berdasarkan waktu pesan terakhir (descending)
            contacts.sort((a, b) -> {
                if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
                if (a.getLastMessageTime() == null) return 1;
                if (b.getLastMessageTime() == null) return -1;
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            });
            logger.debug("Contacts sorted by lastMessageTime");

            logger.info("END getContacts for user {} — returning {} contacts",
                        userId, contacts.size());
            return ResponseEntity.ok(contacts);

        } catch (Exception e) {
            logger.error("ERROR in getContacts for user {}: {}", userId, e.getMessage(), e);
            // meski error, kembalikan list kosong agar frontend tidak gagal total
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("TEST");
    }
}
