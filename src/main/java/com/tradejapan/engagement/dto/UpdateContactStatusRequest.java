package com.tradejapan.engagement.dto;

import com.tradejapan.engagement.model.ContactStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateContactStatusRequest(@NotNull(message = "Status is required") ContactStatus status) {}
