package com.tradejapan.part3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The customer is taken from the login token, so no userId is accepted here. */
public record CreateInquiryRequest(
        @NotNull(message = "vehicleId is required") Long vehicleId,
        @NotBlank(message = "Subject is required")
        @Size(max = 150, message = "Subject must be 150 characters or less") String subject,
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be 2000 characters or less") String message,
        @NotBlank(message = "Destination country is required")
        @Size(max = 100, message = "Destination country must be 100 characters or less") String destinationCountry
) {}
