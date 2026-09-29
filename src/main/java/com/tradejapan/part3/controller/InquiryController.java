package com.tradejapan.part3.controller;

import com.tradejapan.part3.dto.CreateInquiryRequest;
import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryReply;
import com.tradejapan.part3.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inquiry createInquiry(@Valid @RequestBody CreateInquiryRequest request) {
        return inquiryService.createInquiry(request);
    }

    @GetMapping("/{id}")
    public Inquiry getInquiry(@PathVariable Long id) {
        return inquiryService.getInquiry(id);
    }

    @GetMapping("/user/{userId}")
    public List<Inquiry> getMyInquiries(@PathVariable Long userId) {
        return inquiryService.getCustomerInquiries(userId);
    }

    @GetMapping("/{id}/replies")
    public List<InquiryReply> getReplies(@PathVariable Long id) {
        return inquiryService.getReplies(id);
    }
}
