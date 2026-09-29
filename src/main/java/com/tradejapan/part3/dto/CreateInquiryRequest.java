package com.tradejapan.part3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInquiryRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotNull(message = "vehicleId is required") Long vehicleId,
        @NotBlank(message = "subject is required")
        @Size(max = 150, message = "subject must be 150 characters or less") String subject,
        @NotBlank(message = "message is required")
        @Size(max = 2000, message = "message must be 2000 characters or less") String message,
        @NotBlank(message = "destinationCountry is required")
        @Size(max = 100, message = "destinationCountry must be 100 characters or less") String destinationCountry
) {}
