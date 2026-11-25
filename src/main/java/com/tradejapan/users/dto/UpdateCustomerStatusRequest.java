package com.tradejapan.users.dto;

import com.tradejapan.users.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(@NotNull(message = "Status is required") UserStatus status) {}
