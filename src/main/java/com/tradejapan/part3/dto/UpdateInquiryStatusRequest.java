package com.tradejapan.part3.dto;

import com.tradejapan.part3.model.InquiryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInquiryStatusRequest(
        @NotNull(message = "status is required") InquiryStatus status
) {}
