package com.tradejapan.part4.repository;

import com.tradejapan.part4.model.ContactMessage;
import com.tradejapan.part4.model.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    long countByStatus(ContactStatus status);
    long countByHandledBy(Long handledBy);
    List<ContactMessage> findTop15ByHandledByNotNullOrderByHandledAtDesc();
}
