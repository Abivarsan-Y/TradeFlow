package com.tradejapan.orders.controller;

import com.tradejapan.orders.dto.CreatePurchaseRequest;
import com.tradejapan.orders.dto.PurchaseView;
import com.tradejapan.orders.dto.UpdateAdvancePaymentRequest;
import com.tradejapan.orders.dto.UpdatePurchaseStatusRequest;
import com.tradejapan.orders.model.PurchaseStatus;
import com.tradejapan.orders.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Admin / Super Admin only (enforced in SecurityConfig for /api/admin/**). */
@RestController
@RequestMapping("/api/admin/purchases")
public class AdminPurchaseController {

    private final PurchaseService purchaseService;

    public AdminPurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseView createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        return purchaseService.createPurchase(request);
    }

    @GetMapping
    public List<PurchaseView> getAll(@RequestParam(required = false) PurchaseStatus status) {
        return purchaseService.getAdminPurchases(status);
    }

    @PatchMapping("/{id}/status")
    public PurchaseView updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody UpdatePurchaseStatusRequest request) {
        return purchaseService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/advance")
    public PurchaseView updateAdvance(@PathVariable Long id,
                                      @Valid @RequestBody UpdateAdvancePaymentRequest request) {
        return purchaseService.updateAdvance(id, request);
    }

    @PostMapping(value = "/{id}/invoice", consumes = "multipart/form-data")
    public PurchaseView uploadInvoice(@PathVariable Long id,
                                      @RequestParam String invoiceNo,
                                      @RequestPart("file") MultipartFile file) {
        return purchaseService.uploadInvoice(id, invoiceNo, file);
    }
}
