package id.ac.ui.cs.advprog.chat.repository;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByPacilianIdAndDoctorId(Long pacilianId, Long doctorId);

    /**
     * Find all rooms where the user is either a patient or a doctor
     */
    List<ChatRoom> findByPacilianIdOrDoctorId(Long pacilianId, Long doctorId);
}