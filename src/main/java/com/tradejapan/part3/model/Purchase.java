package com.tradejapan.part3.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "purchases",
        uniqueConstraints = @UniqueConstraint(name = "uk_purchase_inquiry", columnNames = "inquiry_id")
)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long purchaseId;

    // Member 2 owns the real users table. This is the buyer/customer ID.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Member 1 owns the real vehicles table.
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    // Admin/Super Admin user who processes this purchase.
    @Column(name = "processed_by", nullable = false)
    private Long processedBy;

    // Final EER: an inquiry may optionally lead to one purchase.
    @Column(name = "inquiry_id", unique = true)
    private Long inquiryId;

    @Column(name = "invoice_no", length = 100)
    private String invoiceNo;

    @Column(name = "invoice_file_url", length = 500)
    private String invoiceFileUrl;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "advance_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseStatus status = PurchaseStatus.PENDING;

    @Column(length = 2000)
    private String notes;

    // where the vehicle is shipped (copied from the inquiry, or chosen by the admin for a direct purchase)
    @Column(name = "destination_country", length = 100)
    private String destinationCountry;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (advancePaid == null) {
            advancePaid = BigDecimal.ZERO;
        }
        if (status == null) {
            status = PurchaseStatus.PENDING;
        }
    }

    public Long getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Long purchaseId) { this.purchaseId = purchaseId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getProcessedBy() { return processedBy; }
    public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }
    public Long getInquiryId() { return inquiryId; }
    public void setInquiryId(Long inquiryId) { this.inquiryId = inquiryId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceFileUrl() { return invoiceFileUrl; }
    public void setInvoiceFileUrl(String invoiceFileUrl) { this.invoiceFileUrl = invoiceFileUrl; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
    public PurchaseStatus getStatus() { return status; }
    public void setStatus(PurchaseStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
}
