package com.tradejapan.part3.service;

import com.tradejapan.part3.dto.CreatePurchaseRequest;
import com.tradejapan.part3.dto.UpdateAdvancePaymentRequest;
import com.tradejapan.part3.dto.UpdatePurchaseStatusRequest;
import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.model.PurchaseStatus;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import com.tradejapan.part3.repository.InquiryRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final InquiryRepository inquiryRepository;
    private final InvoiceStorageService invoiceStorageService;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           InquiryRepository inquiryRepository,
                           InvoiceStorageService invoiceStorageService) {
        this.purchaseRepository = purchaseRepository;
        this.inquiryRepository = inquiryRepository;
        this.invoiceStorageService = invoiceStorageService;
    }

    public Purchase createPurchase(CreatePurchaseRequest request) {
        BigDecimal advance = request.advancePaid() == null ? BigDecimal.ZERO : request.advancePaid();
        if (advance.compareTo(request.totalPrice()) > 0) {
            throw new IllegalArgumentException("advancePaid cannot be greater than totalPrice");
        }

        if (request.inquiryId() != null) {
            if (purchaseRepository.existsByInquiryId(request.inquiryId())) {
                throw new IllegalStateException("This inquiry is already linked to a purchase");
            }
            Inquiry inquiry = inquiryRepository.findById(request.inquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + request.inquiryId()));
            if (!inquiry.getUserId().equals(request.userId())) {
                throw new IllegalArgumentException("Purchase userId must match the linked inquiry userId");
            }
            if (!inquiry.getVehicleId().equals(request.vehicleId())) {
                throw new IllegalArgumentException("Purchase vehicleId must match the linked inquiry vehicleId");
            }
        }

        Purchase purchase = new Purchase();
        purchase.setUserId(request.userId());
        purchase.setVehicleId(request.vehicleId());
        purchase.setProcessedBy(request.processedBy());
        purchase.setInquiryId(request.inquiryId());
        purchase.setTotalPrice(request.totalPrice());
        purchase.setAdvancePaid(advance);
        purchase.setStatus(statusForInitialAdvance(advance, request.totalPrice()));
        purchase.setNotes(clean(request.notes()));
        return purchaseRepository.save(purchase);
    }

    public Purchase getPurchase(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + purchaseId));
    }

    public List<Purchase> getCustomerPurchases(Long userId) {
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Purchase> getAdminPurchases(PurchaseStatus status) {
        return status == null
                ? purchaseRepository.findAllByOrderByCreatedAtDesc()
                : purchaseRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Purchase updateAdvance(Long purchaseId, UpdateAdvancePaymentRequest request) {
        Purchase purchase = getPurchase(purchaseId);
        if (request.advancePaid().compareTo(purchase.getTotalPrice()) > 0) {
            throw new IllegalArgumentException("advancePaid cannot be greater than totalPrice");
        }
        purchase.setAdvancePaid(request.advancePaid());
        if (purchase.getStatus() == PurchaseStatus.PENDING && request.advancePaid().compareTo(BigDecimal.ZERO) > 0) {
            purchase.setStatus(PurchaseStatus.ADVANCE_PAID);
        }
        return purchaseRepository.save(purchase);
    }

    public Purchase updateStatus(Long purchaseId, UpdatePurchaseStatusRequest request) {
        Purchase purchase = getPurchase(purchaseId);
        PurchaseStatus current = purchase.getStatus();
        PurchaseStatus requested = request.status();

        if (requested == current) {
            return purchase;
        }
        if (requested != nextStatus(current)) {
            throw new IllegalStateException("Purchase status must move in order: PENDING -> ADVANCE_PAID -> FULLY_PAID -> DELIVERED");
        }
        if (requested == PurchaseStatus.ADVANCE_PAID && purchase.getAdvancePaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Record an advance payment before setting ADVANCE_PAID");
        }
        if (requested == PurchaseStatus.FULLY_PAID) {
            purchase.setAdvancePaid(purchase.getTotalPrice());
        }

        purchase.setStatus(requested);
        return purchaseRepository.save(purchase);
    }

    public Purchase uploadInvoice(Long purchaseId, String invoiceNo, MultipartFile file) {
        if (invoiceNo == null || invoiceNo.isBlank()) {
            throw new IllegalArgumentException("invoiceNo is required");
        }
        Purchase purchase = getPurchase(purchaseId);
        invoiceStorageService.store(purchaseId, file);
        purchase.setInvoiceNo(invoiceNo.trim());
        purchase.setInvoiceFileUrl("/api/purchases/" + purchaseId + "/invoice");
        return purchaseRepository.save(purchase);
    }

    public Resource loadInvoice(Long purchaseId) {
        Purchase purchase = getPurchase(purchaseId);
        if (purchase.getInvoiceFileUrl() == null || purchase.getInvoiceFileUrl().isBlank()) {
            throw new IllegalArgumentException("Invoice has not been uploaded for this purchase");
        }
        return invoiceStorageService.load(purchaseId);
    }

    private PurchaseStatus statusForInitialAdvance(BigDecimal advance, BigDecimal total) {
        if (advance.compareTo(total) == 0 && total.compareTo(BigDecimal.ZERO) > 0) {
            return PurchaseStatus.FULLY_PAID;
        }
        if (advance.compareTo(BigDecimal.ZERO) > 0) {
            return PurchaseStatus.ADVANCE_PAID;
        }
        return PurchaseStatus.PENDING;
    }

    private PurchaseStatus nextStatus(PurchaseStatus current) {
        return switch (current) {
            case PENDING -> PurchaseStatus.ADVANCE_PAID;
            case ADVANCE_PAID -> PurchaseStatus.FULLY_PAID;
            case FULLY_PAID -> PurchaseStatus.DELIVERED;
            case DELIVERED -> null;
        };
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
