package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.repository.ChatRoomRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ChatRoomService {

    private static final Logger logger = LoggerFactory.getLogger(ChatRoomService.class);

    private final ChatRoomRepository repo;
    private final RestTemplate restTemplate;
    private final Counter chatRoomCreatedCounter;
    private final Timer messageProcessingTimer;

    @Value("${authprofile.service.url:http://localhost:8081/api}")
    private String authProfileUrl;

    // Store token in thread-local for WebSocket context
    private static final ThreadLocal<String> currentAuthToken = new ThreadLocal<>();

    @Autowired
    public ChatRoomService(ChatRoomRepository repo, Counter chatRoomCreatedCounter, Timer messageProcessingTimer) {
        this.repo = repo;
        this.restTemplate = new RestTemplate();
        this.chatRoomCreatedCounter = chatRoomCreatedCounter;
        this.messageProcessingTimer = messageProcessingTimer;
    }

    /**
     * Set the auth token for the current thread (used in WebSocket context)
     */
    public static void setCurrentAuthToken(String token) {
        currentAuthToken.set(token);
    }

    /**
     * Clear the auth token for the current thread
     */
    public static void clearCurrentAuthToken() {
        currentAuthToken.remove();
    }

    /**
     * Creates a chat room between two users, ensuring proper role assignment.
     * Always ensures that pacilianId is the patient and doctorId is the caregiver.
     */
    public ChatRoom createIfNotExists(Long userId1, Long userId2) {
        return messageProcessingTimer.record(() -> {
            logger.info("Creating chat room between users {} and {}", userId1, userId2);

            // Determine which user is the patient and which is the caregiver
            Long pacilianId = null;
            Long doctorId = null;

            // Check if userId1 is a caregiver
            if (isCaregiver(userId1)) {
                doctorId = userId1;
                pacilianId = userId2;
            } else if (isCaregiver(userId2)) {
                doctorId = userId2;
                pacilianId = userId1;
            } else {
                // If neither is a caregiver, treat the first user as patient and second as caregiver
                // (This is a fallback case that shouldn't normally occur)
                logger.warn("Neither user {} nor {} appears to be a caregiver. Using default assignment.", userId1, userId2);
                pacilianId = userId1;
                doctorId = userId2;
            }

            logger.info("Room assignment - Patient: {}, Caregiver: {}", pacilianId, doctorId);

            // Check if room already exists with correct role assignment
            Optional<ChatRoom> existingRoom = repo.findByPacilianIdAndDoctorId(pacilianId, doctorId);

            if (existingRoom.isPresent()) {
                logger.info("Found existing room with ID: {}", existingRoom.get().getId());
                return existingRoom.get();
            }

            // Create new room with proper role assignment
            ChatRoom newRoom = new ChatRoom(pacilianId, doctorId);
            ChatRoom savedRoom = repo.save(newRoom);
            logger.info("Created new room with ID: {}", savedRoom.getId());
            
            // Increment the counter when creating a new room
            chatRoomCreatedCounter.increment();

            return savedRoom;
        });
    }

    /**
     * Check if a user is a caregiver by calling the auth-profile service
     */
    private boolean isCaregiver(Long userId) {
        String authToken = currentAuthToken.get();

        if (authToken == null) {
            logger.warn("No auth token available for user type check of user {}", userId);
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = authProfileUrl + "/caregiver/" + userId;
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.debug("User {} is a caregiver", userId);
                return true;
            }
        } catch (Exception e) {
            logger.debug("User {} is not a caregiver: {}", userId, e.getMessage());
        }

        return false;
    }

    /** Find room by ID */
    public Optional<ChatRoom> find(Long roomId) {
        return repo.findById(roomId);
    }

    /**
     * Find all rooms where user is a participant (either as patient or doctor)
     */
    public List<ChatRoom> findRoomsByUserId(Long userId) {
        return repo.findByPacilianIdOrDoctorId(userId, userId);
    }

    /**
     * Clean up duplicate rooms - utility method to fix existing data
     * This should be called once to clean up any existing duplicate rooms
     */
    @Transactional
    public void cleanupDuplicateRooms() {
        logger.info("Starting cleanup of duplicate chat rooms");

        List<ChatRoom> allRooms = repo.findAll();
        logger.info("Found {} total rooms to check", allRooms.size());

        for (ChatRoom room : allRooms) {
            // Check if there's a reverse duplicate (where roles are swapped)
            Optional<ChatRoom> reverseDuplicate = repo.findByPacilianIdAndDoctorId(
                    room.getDoctorId(), room.getPacilianId()
            );

            if (reverseDuplicate.isPresent() && !reverseDuplicate.get().getId().equals(room.getId())) {
                ChatRoom duplicate = reverseDuplicate.get();
                logger.warn("Found duplicate rooms: {} and {} for users {} and {}",
                        room.getId(), duplicate.getId(), room.getPacilianId(), room.getDoctorId());

                // Keep the room with the lower ID for consistency
                ChatRoom roomToKeep;
                ChatRoom roomToDelete;

                if (room.getId() < duplicate.getId()) {
                    roomToKeep = room;
                    roomToDelete = duplicate;
                } else {
                    roomToKeep = duplicate;
                    roomToDelete = room;
                }

                logger.info("Keeping room {} and deleting room {}", roomToKeep.getId(), roomToDelete.getId());

                // Note: In a production system, you'd want to migrate messages from the deleted room
                // to the kept room before deleting. For simplicity, this just deletes the duplicate.
                repo.delete(roomToDelete);
            }
        }

        logger.info("Cleanup completed");
    }
}