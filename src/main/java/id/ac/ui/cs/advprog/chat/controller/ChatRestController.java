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
        logger.info("Getting contacts for user: {}", userId);

        try {
            // Find all rooms where this user is a participant
            List<ChatRoom> userRooms = roomService.findRoomsByUserId(userId);
            logger.debug("Found {} rooms for user {}", userRooms.size(), userId);

            List<ContactResponse> contacts = new ArrayList<>();

            for (ChatRoom room : userRooms) {
                // Determine who is the contact (the other person in the room)
                Long contactId = userId.equals(room.getPacilianId()) ?
                        room.getDoctorId() : room.getPacilianId();

                logger.debug("Processing contact: {} for room: {}", contactId, room.getId());

                // Get contact details from auth-profile service
                String contactName = authProfileService.getUserName(contactId);

                // Get latest message for preview
                List<ChatMessage> messages = messageService.getMessagesByRoom(room.getId());
                String lastMessage = "";
                if (!messages.isEmpty()) {
                    ChatMessage lastMsg = messages.get(messages.size() - 1);
                    if ("deleted".equals(lastMsg.getStatus())) {
                        lastMessage = "Message deleted";
                    } else {
                        lastMessage = lastMsg.getContent();
                    }
                }

                ContactResponse contact = new ContactResponse();
                contact.setContactId(contactId);
                contact.setContactName(contactName);
                contact.setRoomId(room.getId());
                contact.setLastMessage(lastMessage);
                contact.setLastMessageTime(messages.isEmpty() ? null :
                        messages.get(messages.size() - 1).getTimestamp());

                contacts.add(contact);
                logger.debug("Added contact: {} ({})", contactName, contactId);
            }

            // Sort contacts by last message time (most recent first)
            contacts.sort((a, b) -> {
                if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
                if (a.getLastMessageTime() == null) return 1;
                if (b.getLastMessageTime() == null) return -1;
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            });

            logger.info("Returning {} contacts for user {}", contacts.size(), userId);
            return ResponseEntity.ok(contacts);

        } catch (Exception e) {
            logger.error("Error getting contacts for user {}: ", userId, e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}