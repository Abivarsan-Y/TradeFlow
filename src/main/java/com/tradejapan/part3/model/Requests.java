package com.tradejapan.part3.model;
import jakarta.validation.constraints.*;
import com.tradejapan.part3.entity.*;
import java.math.BigDecimal;
// Request models (DTOs), not database tables. Never accept customer/author IDs for inquiry ownership.
public final class Requests {
 private Requests() {}
 public record CreateInquiry(@NotNull @Positive Long vehicleId,
   @NotBlank @Size(max=200) String subject, @NotBlank @Size(max=10000) String message,
   @NotBlank @Size(max=100) String destinationCountry) {}
 public record Reply(@NotBlank @Size(max=10000) String message) {}
 public record ChangeInquiryStatus(@NotNull InquiryStatus status) {}
 public record CreatePurchase(@NotNull @Positive Long customerId, @NotNull @Positive Long vehicleId,
   @Positive Long inquiryId, @NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal totalPrice,
   @Size(max=10000) String notes) {}
 public record ChangePurchaseStatus(@NotNull PurchaseStatus status,
   @DecimalMin("0.00") @Digits(integer=12,fraction=2) BigDecimal advancePaid) {}
}
