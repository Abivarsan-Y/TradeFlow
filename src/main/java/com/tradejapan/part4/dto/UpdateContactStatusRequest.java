package com.tradejapan.part4.dto;

import com.tradejapan.part4.model.ContactStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateContactStatusRequest(@NotNull(message = "Status is required") ContactStatus status) {}
