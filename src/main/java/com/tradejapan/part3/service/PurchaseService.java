package com.tradejapan.part3.service;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;
import com.tradejapan.part2.repository.VehicleRepository;
import com.tradejapan.part3.dto.CreatePurchaseRequest;
import com.tradejapan.part3.dto.PurchaseView;
import com.tradejapan.part3.dto.UpdateAdvancePaymentRequest;
import com.tradejapan.part3.dto.UpdatePurchaseStatusRequest;
import com.tradejapan.part3.exception.ResourceNotFoundException;
import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.model.PurchaseStatus;
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
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final CurrentUserService currentUserService;
    private final ViewMapper viewMapper;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           InquiryRepository inquiryRepository,
                           InvoiceStorageService invoiceStorageService,
                           UserRepository userRepository,
                           VehicleRepository vehicleRepository,
                           CurrentUserService currentUserService,
                           ViewMapper viewMapper) {
        this.purchaseRepository = purchaseRepository;
        this.inquiryRepository = inquiryRepository;
        this.invoiceStorageService = invoiceStorageService;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.currentUserService = currentUserService;
        this.viewMapper = viewMapper;
    }

    // ---------- admin: create ----------
    public PurchaseView createPurchase(CreatePurchaseRequest request) {
        User admin = currentUserService.requireUser();
        BigDecimal advance = request.advancePaid() == null ? BigDecimal.ZERO : request.advancePaid();
        if (advance.compareTo(request.totalPrice()) > 0) {
            throw new IllegalArgumentException("Advance paid cannot be greater than the total price");
        }

        Long buyerId;
        Long vehicleId;
        String destination;
        if (request.inquiryId() != null) {
            if (purchaseRepository.existsByInquiryId(request.inquiryId())) {
                throw new IllegalStateException("This inquiry is already linked to a purchase");
            }
            Inquiry inquiry = inquiryRepository.findById(request.inquiryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + request.inquiryId()));
            buyerId = inquiry.getUserId();
            vehicleId = inquiry.getVehicleId();
            destination = inquiry.getDestinationCountry();
            if (request.vehicleId() != null && !request.vehicleId().equals(vehicleId)) {
                throw new IllegalArgumentException("Vehicle must match the linked inquiry");
            }
        } else {
            if (request.buyerEmail() == null || request.buyerEmail().isBlank()) {
                throw new IllegalArgumentException("Buyer email is required when no inquiry is linked");
            }
            if (request.vehicleId() == null) {
                throw new IllegalArgumentException("Vehicle ID is required when no inquiry is linked");
            }
            User buyer = userRepository.findByEmail(request.buyerEmail().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("No customer found with email " + request.buyerEmail().trim()));
            if (buyer.getRole() != Role.ROLE_CUSTOMER) {
                throw new IllegalArgumentException("The buyer must be a customer account");
            }
            buyerId = buyer.getId();
            vehicleId = request.vehicleId();
            destination = clean(request.destinationCountry());
        }

        if (!vehicleRepository.existsById(vehicleId.intValue())) {
            throw new ResourceNotFoundException("Vehicle not found: " + vehicleId);
        }

        Purchase purchase = new Purchase();
        purchase.setUserId(buyerId);
        purchase.setVehicleId(vehicleId);
        purchase.setProcessedBy(admin.getId());
        purchase.setInquiryId(request.inquiryId());
        purchase.setTotalPrice(request.totalPrice());
        purchase.setAdvancePaid(advance);
        purchase.setStatus(statusForInitialAdvance(advance, request.totalPrice()));
        purchase.setDestinationCountry(destination);
        purchase.setNotes(clean(request.notes()));
        return viewMapper.purchase(purchaseRepository.save(purchase));
    }

    // ---------- reads ----------
    public List<PurchaseView> getMyPurchases() {
        User user = currentUserService.requireUser();
        return viewMapper.purchases(purchaseRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    public List<PurchaseView> getAdminPurchases(PurchaseStatus status) {
        List<Purchase> list = status == null
                ? purchaseRepository.findAllByOrderByCreatedAtDesc()
                : purchaseRepository.findByStatusOrderByCreatedAtDesc(status);
        return viewMapper.purchases(list);
    }

    /** Customers may only open their own purchases; admins may open any. */
    private Purchase findAccessible(Long purchaseId) {
        User user = currentUserService.requireUser();
        Purchase purchase = findPurchase(purchaseId);
        if (!currentUserService.isAdmin(user) && !purchase.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("Purchase not found: " + purchaseId);
        }
        return purchase;
    }

    private Purchase findPurchase(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + purchaseId));
    }

    public PurchaseView getPurchase(Long purchaseId) {
        return viewMapper.purchase(findAccessible(purchaseId));
    }

    public String invoiceDownloadName(Long purchaseId) {
        Purchase purchase = findAccessible(purchaseId);
        return purchase.getInvoiceNo() == null || purchase.getInvoiceNo().isBlank()
                ? "invoice-" + purchaseId + ".pdf"
                : purchase.getInvoiceNo() + ".pdf";
    }

    public Resource loadInvoice(Long purchaseId) {
        Purchase purchase = findAccessible(purchaseId);
        if (purchase.getInvoiceFileUrl() == null || purchase.getInvoiceFileUrl().isBlank()) {
            throw new IllegalArgumentException("Invoice has not been uploaded for this purchase");
        }
        return invoiceStorageService.load(purchaseId);
    }

    // ---------- admin: updates ----------
    public PurchaseView updateAdvance(Long purchaseId, UpdateAdvancePaymentRequest request) {
        Purchase purchase = findPurchase(purchaseId);
        if (purchase.getStatus() == PurchaseStatus.FULLY_PAID || purchase.getStatus() == PurchaseStatus.DELIVERED) {
            throw new IllegalStateException("Advance payment cannot be changed after the purchase is fully paid");
        }
        if (request.advancePaid().compareTo(purchase.getTotalPrice()) > 0) {
            throw new IllegalArgumentException("Advance paid cannot be greater than the total price");
        }
        purchase.setAdvancePaid(request.advancePaid());
        if (request.advancePaid().compareTo(BigDecimal.ZERO) == 0) {
            purchase.setStatus(PurchaseStatus.PENDING);
        } else if (purchase.getStatus() == PurchaseStatus.PENDING) {
            purchase.setStatus(PurchaseStatus.ADVANCE_PAID);
        }
        return viewMapper.purchase(purchaseRepository.save(purchase));
    }

    public PurchaseView updateStatus(Long purchaseId, UpdatePurchaseStatusRequest request) {
        Purchase purchase = findPurchase(purchaseId);
        PurchaseStatus current = purchase.getStatus();
        PurchaseStatus requested = request.status();
        if (requested == current) {
            return viewMapper.purchase(purchase);
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
        return viewMapper.purchase(purchaseRepository.save(purchase));
    }

    public PurchaseView uploadInvoice(Long purchaseId, String invoiceNo, MultipartFile file) {
        if (invoiceNo == null || invoiceNo.isBlank()) {
            throw new IllegalArgumentException("Invoice number is required");
        }
        Purchase purchase = findPurchase(purchaseId);
        invoiceStorageService.store(purchaseId, file);
        purchase.setInvoiceNo(invoiceNo.trim());
        purchase.setInvoiceFileUrl("/api/purchases/" + purchaseId + "/invoice");
        return viewMapper.purchase(purchaseRepository.save(purchase));
    }

    // ---------- helpers ----------
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
