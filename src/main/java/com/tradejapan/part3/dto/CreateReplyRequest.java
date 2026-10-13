package com.tradejapan.part3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The author is the logged-in admin, so no userId is accepted here. */
public record CreateReplyRequest(
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be 2000 characters or less") String message
) {}
