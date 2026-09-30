package com.tradejapan.function4.service;

import com.tradejapan.function4.entity.ContactMessage;
import com.tradejapan.function4.entity.ContactStatus;
import com.tradejapan.function4.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ContactMessageService {
    private final ContactMessageRepository repository;

    public ContactMessageService(ContactMessageRepository repository) {
        this.repository = repository;
    }

    public ContactMessage submit(ContactMessage message) {
        message.setMessageId(null);
        message.setStatus(ContactStatus.NEW);
        return repository.save(message);
    }

    public List<ContactMessage> getAll() {
        return repository.findAll();
    }

    public ContactMessage updateStatus(Long id, ContactStatus status) {
        ContactMessage message = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Contact message not found: " + id));
        message.setStatus(status);
        return repository.save(message);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
