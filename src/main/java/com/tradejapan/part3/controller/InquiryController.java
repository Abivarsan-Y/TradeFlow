package com.tradejapan.part3.controller;

import com.tradejapan.part3.dto.CreateInquiryRequest;
import com.tradejapan.part3.dto.CreateReplyRequest;
import com.tradejapan.part3.dto.InquiryView;
import com.tradejapan.part3.dto.ReplyView;
import com.tradejapan.part3.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Customer side. Every call is scoped to the logged-in user. */
@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryView createInquiry(@Valid @RequestBody CreateInquiryRequest request) {
        return inquiryService.createInquiry(request);
    }

    @GetMapping("/me")
    public List<InquiryView> getMyInquiries() {
        return inquiryService.getMyInquiries();
    }

    @GetMapping("/{id}")
    public InquiryView getInquiry(@PathVariable Long id) {
        return inquiryService.getInquiry(id);
    }

    @GetMapping("/{id}/replies")
    public List<ReplyView> getReplies(@PathVariable Long id) {
        return inquiryService.getReplies(id);
    }

    /** The customer writes another message in their own chat (any number of times). */
    @PostMapping("/{id}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    public ReplyView addCustomerReply(@PathVariable Long id, @Valid @RequestBody CreateReplyRequest request) {
        return inquiryService.addCustomerReply(id, request);
    }
}
