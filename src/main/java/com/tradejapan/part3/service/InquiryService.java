package com.tradejapan.part3.service;

import com.tradejapan.part3.dto.CreateInquiryRequest;
import com.tradejapan.part3.dto.CreateReplyRequest;
import com.tradejapan.part3.dto.UpdateInquiryStatusRequest;
import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryReply;
import com.tradejapan.part3.model.InquiryStatus;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import com.tradejapan.part3.repository.InquiryReplyRepository;
import com.tradejapan.part3.repository.InquiryRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final PurchaseRepository purchaseRepository;

    public InquiryService(InquiryRepository inquiryRepository,
                          InquiryReplyRepository inquiryReplyRepository,
                          PurchaseRepository purchaseRepository) {
        this.inquiryRepository = inquiryRepository;
        this.inquiryReplyRepository = inquiryReplyRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public Inquiry createInquiry(CreateInquiryRequest request) {
        Inquiry inquiry = new Inquiry();
        inquiry.setUserId(request.userId());
        inquiry.setVehicleId(request.vehicleId());
        inquiry.setSubject(request.subject().trim());
        inquiry.setMessage(request.message().trim());
        inquiry.setDestinationCountry(request.destinationCountry().trim());
        inquiry.setStatus(InquiryStatus.NEW);
        return inquiryRepository.save(inquiry);
    }

    public Inquiry getInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
    }

    public List<Inquiry> getCustomerInquiries(Long userId) {
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Inquiry> getAdminInquiries(InquiryStatus status) {
        if (status == null) {
            return inquiryRepository.findAllByOrderByCreatedAtDesc();
        }
        return inquiryRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Inquiry updateStatus(Long inquiryId, UpdateInquiryStatusRequest request) {
        Inquiry inquiry = getInquiry(inquiryId);
        inquiry.setStatus(request.status());
        return inquiryRepository.save(inquiry);
    }

    public InquiryReply addReply(Long inquiryId, CreateReplyRequest request) {
        getInquiry(inquiryId);
        InquiryReply reply = new InquiryReply();
        reply.setInquiryId(inquiryId);
        reply.setUserId(request.userId());
        reply.setMessage(request.message().trim());
        return inquiryReplyRepository.save(reply);
    }

    public List<InquiryReply> getReplies(Long inquiryId) {
        getInquiry(inquiryId);
        return inquiryReplyRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Inquiry inquiry = getInquiry(inquiryId);
        if (purchaseRepository.existsByInquiryId(inquiryId)) {
            throw new IllegalStateException("Cannot delete an inquiry that is linked to a purchase");
        }
        inquiryReplyRepository.deleteByInquiryId(inquiryId);
        inquiryRepository.delete(inquiry);
    }
}
