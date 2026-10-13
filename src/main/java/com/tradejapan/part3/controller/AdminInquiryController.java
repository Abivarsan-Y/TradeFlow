package com.tradejapan.part3.controller;

import com.tradejapan.part3.dto.CreateReplyRequest;
import com.tradejapan.part3.dto.InquiryView;
import com.tradejapan.part3.dto.ReplyView;
import com.tradejapan.part3.dto.UpdateInquiryStatusRequest;
import com.tradejapan.part3.model.InquiryStatus;
import com.tradejapan.part3.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin / Super Admin only (enforced in SecurityConfig for /api/admin/**). */
@RestController
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    public AdminInquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @GetMapping
    public List<InquiryView> getAll(@RequestParam(required = false) InquiryStatus status) {
        return inquiryService.getAdminInquiries(status);
    }

    @PatchMapping("/{id}/status")
    public InquiryView updateStatus(@PathVariable Long id,
                                    @Valid @RequestBody UpdateInquiryStatusRequest request) {
        return inquiryService.updateStatus(id, request);
    }

    @PostMapping("/{id}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplyView addReply(@PathVariable Long id,
                              @Valid @RequestBody CreateReplyRequest request) {
        return inquiryService.addReply(id, request);
    }

    /** Called when an admin opens a chat: clears the "unread" counter. */
    @PostMapping("/{id}/read")
    public InquiryView markRead(@PathVariable Long id) {
        return inquiryService.markRead(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
    }
}
