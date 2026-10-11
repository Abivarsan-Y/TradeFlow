package com.tradejapan.part4.controller;

import com.tradejapan.part4.dto.ContactMessageView;
import com.tradejapan.part4.dto.CreateContactMessageRequest;
import com.tradejapan.part4.dto.UpdateContactStatusRequest;
import com.tradejapan.part4.model.ContactMessage;
import com.tradejapan.part4.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContactMessageController {

    private final ContactMessageService service;

    public ContactMessageController(
            ContactMessageService service) {

        this.service = service;
    }

    /**
     * PUBLIC
     * Submit Contact Us message.
     */
    @PostMapping("/contact-messages")
    public ResponseEntity<ContactMessageView> submit(
            @Valid
            @RequestBody
            CreateContactMessageRequest request) {

        ContactMessage message =
                service.submit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toView(message));
    }

    /**
     * ADMIN
     * View all contact messages.
     */
    @GetMapping("/admin/contact-messages")
    public List<ContactMessageView> getAll() {

        return service
                .getAll()
                .stream()
                .map(this::toView)
                .toList();
    }

    /**
     * ADMIN
     * Change message status.
     */
    @PatchMapping(
            "/admin/contact-messages/{id}/status"
    )
    public ContactMessageView updateStatus(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateContactStatusRequest request) {

        ContactMessage message =
                service.updateStatus(
                        id,
                        request.status()
                );

        return toView(message);
    }

    /**
     * ADMIN
     * Delete contact message.
     */
    @DeleteMapping(
            "/admin/contact-messages/{id}"
    )
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    /**
     * Convert entity into API response.
     *
     * handledByName remains null until
     * shared User/Admin authentication
     * is integrated.
     */
    private ContactMessageView toView(
            ContactMessage message) {

        return new ContactMessageView(
                message.getMessageId(),
                message.getName(),
                message.getEmail(),
                message.getSubject(),
                message.getMessage(),
                message.getStatus(),
                message.getCreatedAt(),
                null,
                message.getHandledAt()
        );
    }
}