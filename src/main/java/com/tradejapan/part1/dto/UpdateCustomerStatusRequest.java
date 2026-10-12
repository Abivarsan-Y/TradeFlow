package com.tradejapan.cms.dto;

import com.tradejapan.cms.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(@NotNull(message = "Status is required") UserStatus status) {}
