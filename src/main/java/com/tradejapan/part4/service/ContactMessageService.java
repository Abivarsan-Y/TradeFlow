package com.tradejapan.part4.service;

import com.tradejapan.part4.dto.CreateContactMessageRequest;
import com.tradejapan.part4.model.ContactMessage;
import com.tradejapan.part4.model.ContactStatus;
import com.tradejapan.part4.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactMessageService {

    private final ContactMessageRepository repository;

    public ContactMessageService(
            ContactMessageRepository repository) {

        this.repository = repository;
    }

    /**
     * Public user submits a Contact Us message.
     */
    @Transactional
    public ContactMessage submit(
            CreateContactMessageRequest request) {

        ContactMessage message =
                new ContactMessage();

        message.setName(
                request.name().trim()
        );

        message.setEmail(
                request.email().trim()
        );

        message.setSubject(
                request.subject().trim()
        );

        message.setMessage(
                request.message().trim()
        );

        message.setStatus(
                ContactStatus.NEW
        );

        return repository.save(message);
    }

    /**
     * Admin retrieves all messages,
     * newest first.
     */
    @Transactional(readOnly = true)
    public List<ContactMessage> getAll() {

        return repository
                .findAllByOrderByCreatedAtDesc();
    }

    /**
     * Admin changes the status.
     *
     * adminId will later come from JWT authentication.
     */
    @Transactional
    public ContactMessage updateStatus(
            Long id,
            ContactStatus status,
            Long adminId) {

        ContactMessage message =
                findMessageOrThrow(id);

        message.setStatus(status);

        message.setHandledBy(adminId);

        message.setHandledAt(
                LocalDateTime.now()
        );

        return repository.save(message);
    }

    /**
     * Temporary overload until shared
     * authentication is integrated.
     */
    @Transactional
    public ContactMessage updateStatus(
            Long id,
            ContactStatus status) {

        return updateStatus(
                id,
                status,
                null
        );
    }

    /**
     * Delete a contact message.
     */
    @Transactional
    public void delete(Long id) {

        ContactMessage message =
                findMessageOrThrow(id);

        repository.delete(message);
    }

    /**
     * Count open/new messages.
     * Can later be used by the dashboard.
     */
    @Transactional(readOnly = true)
    public long countNewMessages() {

        return repository
                .countByStatus(
                        ContactStatus.NEW
                );
    }

    /**
     * Internal lookup helper.
     */
    private ContactMessage findMessageOrThrow(
            Long id) {

        return repository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Contact message not found: "
                                        + id
                        )
                );
    }
}