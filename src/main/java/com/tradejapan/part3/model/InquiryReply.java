package com.tradejapan.part3.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_replies")
public class InquiryReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private Long replyId;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 2000)
    private String message;

    // true = written by the Trade Japan team, false = written by the customer
    // (empty on old rows -> decided by the author's role)
    @Column(name = "from_admin")
    private Boolean fromAdmin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getReplyId() { return replyId; }
    public void setReplyId(Long replyId) { this.replyId = replyId; }
    public Long getInquiryId() { return inquiryId; }
    public void setInquiryId(Long inquiryId) { this.inquiryId = inquiryId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getFromAdmin() { return fromAdmin; }
    public void setFromAdmin(Boolean fromAdmin) { this.fromAdmin = fromAdmin; }
}
