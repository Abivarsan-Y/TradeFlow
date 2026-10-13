package com.tradejapan.part3.service;

import com.tradejapan.part1.model.User;
import com.tradejapan.part2.repository.VehicleRepository;
import com.tradejapan.part3.dto.CreateInquiryRequest;
import com.tradejapan.part3.dto.CreateReplyRequest;
import com.tradejapan.part3.dto.InquiryView;
import com.tradejapan.part3.dto.ReplyView;
import com.tradejapan.part3.dto.UpdateInquiryStatusRequest;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryReply;
import com.tradejapan.part3.model.InquiryStatus;
import com.tradejapan.part3.repository.InquiryReplyRepository;
import com.tradejapan.part3.repository.InquiryRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final PurchaseRepository purchaseRepository;
    private final VehicleRepository vehicleRepository;
    private final CurrentUserService currentUserService;
    private final ViewMapper viewMapper;

    public InquiryService(InquiryRepository inquiryRepository,
                          InquiryReplyRepository inquiryReplyRepository,
                          PurchaseRepository purchaseRepository,
                          VehicleRepository vehicleRepository,
                          CurrentUserService currentUserService,
                          ViewMapper viewMapper) {
        this.inquiryRepository = inquiryRepository;
        this.inquiryReplyRepository = inquiryReplyRepository;
        this.purchaseRepository = purchaseRepository;
        this.vehicleRepository = vehicleRepository;
        this.currentUserService = currentUserService;
        this.viewMapper = viewMapper;
    }

    // ---------- customer ----------
    public InquiryView createInquiry(CreateInquiryRequest request) {
        User user = currentUserService.requireUser();
        if (!vehicleRepository.existsById(request.vehicleId().intValue())) {
            throw new ResourceNotFoundException("Vehicle not found: " + request.vehicleId());
        }
        Inquiry inquiry = new Inquiry();
        inquiry.setUserId(user.getId());
        inquiry.setVehicleId(request.vehicleId());
        inquiry.setSubject(request.subject().trim());
        inquiry.setMessage(request.message().trim());
        inquiry.setDestinationCountry(request.destinationCountry().trim());
        inquiry.setStatus(InquiryStatus.NEW);
        return viewMapper.inquiry(inquiryRepository.save(inquiry));
    }

    public List<InquiryView> getMyInquiries() {
        User user = currentUserService.requireUser();
        return viewMapper.inquiries(inquiryRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    /** Customers may only open their own inquiries; admins may open any. */
    private Inquiry findAccessible(Long inquiryId) {
        User user = currentUserService.requireUser();
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
        if (!currentUserService.isAdmin(user) && !inquiry.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("Inquiry not found: " + inquiryId);
        }
        return inquiry;
    }

    public InquiryView getInquiry(Long inquiryId) {
        return viewMapper.inquiry(findAccessible(inquiryId));
    }

    public List<ReplyView> getReplies(Long inquiryId) {
        findAccessible(inquiryId);
        return viewMapper.replies(inquiryReplyRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId));
    }

    // ---------- admin ----------
    public List<InquiryView> getAdminInquiries(InquiryStatus status) {
        List<Inquiry> list = status == null
                ? inquiryRepository.findAllByOrderByCreatedAtDesc()
                : inquiryRepository.findByStatusOrderByCreatedAtDesc(status);
        return viewMapper.inquiries(list);
    }

    public InquiryView updateStatus(Long inquiryId, UpdateInquiryStatusRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
        inquiry.setStatus(request.status());
        return viewMapper.inquiry(inquiryRepository.save(inquiry));
    }

    /** Admin reply. The admin has obviously seen the chat, so it also counts as read. */
    public ReplyView addReply(Long inquiryId, CreateReplyRequest request) {
        User admin = currentUserService.requireUser();
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
        InquiryReply reply = new InquiryReply();
        reply.setInquiryId(inquiryId);
        reply.setUserId(admin.getId());
        reply.setFromAdmin(true);
        reply.setMessage(request.message().trim());
        InquiryReply saved = inquiryReplyRepository.save(reply);

        inquiry.setAdminLastReadAt(LocalDateTime.now());
        // The customer's "Pending" badge turns into "Answered" once the team replies.
        if (inquiry.getStatus() == InquiryStatus.NEW) {
            inquiry.setStatus(InquiryStatus.CONTACTED);
        }
        inquiryRepository.save(inquiry);
        return viewMapper.reply(saved);
    }

    /** Customer reply: only the customer who owns the inquiry may write in it. Can be sent any number of times. */
    public ReplyView addCustomerReply(Long inquiryId, CreateReplyRequest request) {
        User user = currentUserService.requireUser();
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
        if (!inquiry.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("Inquiry not found: " + inquiryId);
        }
        InquiryReply reply = new InquiryReply();
        reply.setInquiryId(inquiryId);
        reply.setUserId(user.getId());
        reply.setFromAdmin(false);
        reply.setMessage(request.message().trim());
        return viewMapper.reply(inquiryReplyRepository.save(reply));
    }

    /** An admin opened the chat: everything the customer wrote so far is now read. */
    public InquiryView markRead(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
        inquiry.setAdminLastReadAt(LocalDateTime.now());
        return viewMapper.inquiry(inquiryRepository.save(inquiry));
    }

    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));
        if (purchaseRepository.existsByInquiryId(inquiryId)) {
            throw new IllegalStateException("Cannot delete an inquiry that is linked to a purchase");
        }
        inquiryReplyRepository.deleteByInquiryId(inquiryId);
        inquiryRepository.delete(inquiry);
    }
}
