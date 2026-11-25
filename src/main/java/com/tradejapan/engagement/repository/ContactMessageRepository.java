package com.tradejapan.engagement.repository;

import com.tradejapan.engagement.model.ContactMessage;
import com.tradejapan.engagement.model.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    long countByStatus(ContactStatus status);
    long countByHandledBy(Long handledBy);
    List<ContactMessage> findTop15ByHandledByNotNullOrderByHandledAtDesc();
}
