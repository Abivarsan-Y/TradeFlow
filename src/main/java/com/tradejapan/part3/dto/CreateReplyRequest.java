package com.tradejapan.part3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReplyRequest(
        @NotNull(message = "userId is required") Long userId,
        @NotBlank(message = "message is required")
        @Size(max = 2000, message = "message must be 2000 characters or less") String message
) {}
