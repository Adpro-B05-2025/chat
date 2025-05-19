// src/main/java/id/ac/ui/cs/advprog/chat/model/ChatRoom.java
package id.ac.ui.cs.advprog.chat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pacilianId;
    private Long doctorId;

    public ChatRoom() {
    }

    public ChatRoom(Long id, Long pacilianId, Long doctorId) {
        this.id = id;
        this.pacilianId = pacilianId;
        this.doctorId = doctorId;
    }

    public ChatRoom(Long pacilianId, Long doctorId) {
        this.pacilianId = pacilianId;
        this.doctorId = doctorId;
    }

    public Long getId() {
        return id;
    }

    public Long getPacilianId() {
        return pacilianId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPacilianId(Long pacilianId) {
        this.pacilianId = pacilianId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
