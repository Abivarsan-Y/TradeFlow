package com.tradejapan.engagement.controller;

import com.tradejapan.engagement.dto.ContactMessageView;
import com.tradejapan.engagement.dto.CreateContactMessageRequest;
import com.tradejapan.engagement.dto.UpdateContactStatusRequest;
import com.tradejapan.engagement.model.ContactMessage;
import com.tradejapan.users.service.AdminNames;
import com.tradejapan.engagement.service.ContactMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ContactMessageController {

    private final ContactMessageService service;
    private final AdminNames adminNames;

    public ContactMessageController(
            ContactMessageService service,
            AdminNames adminNames) {

        this.service = service;
        this.adminNames = adminNames;
    }

    @PostMapping("/contact-messages")
    public ResponseEntity<ContactMessageView> submit(
            @Valid @RequestBody CreateContactMessageRequest request) {

        ContactMessage message =
                service.submit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toView(message));
    }

    @GetMapping("/admin/contact-messages")
    public List<ContactMessageView> getAll() {

        return service.getAll()
                .stream()
                .map(this::toView)
                .toList();
    }

    @PatchMapping("/admin/contact-messages/{id}/status")
    public ContactMessageView updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactStatusRequest request) {

        Long adminId =
                adminNames.currentAdminId();

        ContactMessage message =
                service.updateStatus(
                        id,
                        request.status(),
                        adminId
                );

        return toView(message);
    }

    @DeleteMapping("/admin/contact-messages/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

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
                adminNames.nameOf(
                        message.getHandledBy()
                ),
                message.getHandledAt()
        );
    }
}