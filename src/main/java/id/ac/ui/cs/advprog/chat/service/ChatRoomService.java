package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository repo;

    public ChatRoomService(ChatRoomRepository repo) {
        this.repo = repo;
    }

    /**
     * Cari room (pacilian, doctor).
     * Jika belum ada, bikin baru dan simpan.
     */
    public ChatRoom createIfNotExists(Long pacilianId, Long doctorId) {
        return repo.findByPacilianIdAndDoctorId(pacilianId, doctorId)
                .orElseGet(() -> repo.save(new ChatRoom(pacilianId, doctorId)));
    }

    /** Cari room by ID */
    public Optional<ChatRoom> find(Long roomId) {
        return repo.findById(roomId);
    }
}