package com.tradejapan.part4.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(nullable=false, length=100) private String name;
    @Column(nullable=false, length=150) private String email;
    @Column(nullable=false, length=200) private String subject;
    @Column(nullable=false, columnDefinition="TEXT") private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ContactStatus status = ContactStatus.NEW;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ContactStatus.NEW;
    }

    public Long getMessageId(){return messageId;}
    public void setMessageId(Long v){messageId=v;}
    public String getName(){return name;}
    public void setName(String v){name=v;}
    public String getEmail(){return email;}
    public void setEmail(String v){email=v;}
    public String getSubject(){return subject;}
    public void setSubject(String v){subject=v;}
    public String getMessage(){return message;}
    public void setMessage(String v){message=v;}
    public ContactStatus getStatus(){return status;}
    public void setStatus(ContactStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
