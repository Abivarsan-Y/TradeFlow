package com.tradejapan.function4.controller;

import com.tradejapan.function4.entity.ContactMessage;
import com.tradejapan.function4.entity.ContactStatus;
import com.tradejapan.function4.service.ContactMessageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ContactMessageController {
    private final ContactMessageService service;

    public ContactMessageController(ContactMessageService service) {
        this.service = service;
    }

    @PostMapping("/contact-messages")
    public ResponseEntity<ContactMessage> submit(@RequestBody ContactMessage message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(message));
    }

    @GetMapping("/admin/contact-messages")
    public List<ContactMessage> getAll() {
        return service.getAll();
    }

    @PatchMapping("/admin/contact-messages/{id}/status")
    public ContactMessage updateStatus(@PathVariable Long id,
                                       @RequestParam ContactStatus status) {
        return service.updateStatus(id, status);
    }

    @DeleteMapping("/admin/contact-messages/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
