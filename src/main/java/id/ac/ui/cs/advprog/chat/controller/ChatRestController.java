package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import id.ac.ui.cs.advprog.chat.service.AuthProfileService;
import id.ac.ui.cs.advprog.chat.dto.ContactResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatRestController {

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
        try {
            List<ChatRoom> userRooms = roomService.findRoomsByUserId(userId);
            List<ContactResponse> contacts = new ArrayList<>();

            for (ChatRoom room : userRooms) {
                Long contactId = userId.equals(room.getPacilianId())
                        ? room.getDoctorId()
                        : room.getPacilianId();

                String contactName = authProfileService.getUserName(contactId);
                List<ChatMessage> messages = messageService.getMessagesByRoom(room.getId());

                String lastMessage = "";
                if (!messages.isEmpty()) {
                    ChatMessage lastMsg = messages.get(messages.size() - 1);
                    lastMessage = "deleted".equalsIgnoreCase(lastMsg.getStatus())
                            ? "Message deleted"
                            : "Message preview loaded";
                }

                ContactResponse contact = new ContactResponse();
                contact.setContactId(contactId);
                contact.setContactName(contactName);
                contact.setRoomId(room.getId());
                contact.setLastMessage(lastMessage);
                contact.setLastMessageTime(
                        messages.isEmpty() ? null : messages.get(messages.size() - 1).getTimestamp()
                );

                contacts.add(contact);
            }

            contacts.sort((a, b) -> {
                if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) return 0;
                if (a.getLastMessageTime() == null) return 1;
                if (b.getLastMessageTime() == null) return -1;
                return b.getLastMessageTime().compareTo(a.getLastMessageTime());
            });

            return ResponseEntity.ok(contacts);

        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}
