package com.tradejapan.part3.controller;

import com.tradejapan.part3.dto.PurchaseView;
import com.tradejapan.part3.service.PurchaseService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Customer side. Every call is scoped to the logged-in user (admins may open any purchase). */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/me")
    public List<PurchaseView> getMyPurchases() {
        return purchaseService.getMyPurchases();
    }

    @GetMapping("/{id}")
    public PurchaseView getPurchase(@PathVariable Long id) {
        return purchaseService.getPurchase(id);
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable Long id) {
        String fileName = purchaseService.invoiceDownloadName(id);
        Resource resource = purchaseService.loadInvoice(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(fileName, StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }
}
