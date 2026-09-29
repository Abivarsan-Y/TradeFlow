package com.tradejapan.part3.model;
import com.tradejapan.part3.entity.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
public final class Responses {
 private Responses() {}
 public record InquiryView(Long id, Long customerId, Long vehicleId, String vehicleReference,
   String subject, String message, String destinationCountry, InquiryStatus status, Instant createdAt) {
  public static InquiryView from(Inquiry i) { return new InquiryView(i.getId(),i.getCustomer().getId(),i.getVehicle().getId(),i.getVehicle().getRefNo(),i.getSubject(),i.getMessage(),i.getDestinationCountry(),i.getStatus(),i.getCreatedAt()); }
 }
 public record ReplyView(Long id, Long inquiryId, Long authorId, String authorRole, String message, Instant createdAt) {
  public static ReplyView from(InquiryReply r) { return new ReplyView(r.getId(),r.getInquiry().getId(),r.getAuthor().getId(),r.getAuthor().getRole().name(),r.getMessage(),r.getCreatedAt()); }
 }
 public record PurchaseView(Long id, Long customerId, Long vehicleId, Long inquiryId, Long processedBy,
   String invoiceNo, String invoiceFileUrl, BigDecimal totalPrice, BigDecimal advancePaid,
   PurchaseStatus status, String notes, Instant createdAt) {
  public static PurchaseView from(Purchase p) { return new PurchaseView(p.getId(),p.getCustomer().getId(),p.getVehicle().getId(),p.getInquiry()==null?null:p.getInquiry().getId(),p.getProcessedBy().getId(),p.getInvoiceNo(),p.getInvoiceFileUrl(),p.getTotalPrice(),p.getAdvancePaid(),p.getStatus(),p.getNotes(),p.getCreatedAt()); }
 }
 public record PageView<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
  public static <T> PageView<T> from(Page<T> p) { return new PageView<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages()); }
 }
}
