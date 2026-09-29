package com.tradejapan.part3.controller;

import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.service.PurchaseService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/user/{userId}")
    public List<Purchase> getMyPurchases(@PathVariable Long userId) {
        return purchaseService.getCustomerPurchases(userId);
    }

    @GetMapping("/{id}")
    public Purchase getPurchase(@PathVariable Long id) {
        return purchaseService.getPurchase(id);
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long id) {
        Purchase purchase = purchaseService.getPurchase(id);
        Resource resource = purchaseService.loadInvoice(id);
        String fileName = purchase.getInvoiceNo() == null
                ? "invoice-" + id + ".pdf"
                : purchase.getInvoiceNo() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }
}
