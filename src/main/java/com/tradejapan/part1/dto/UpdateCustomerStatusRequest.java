package com.tradejapan.part1.dto;

import com.tradejapan.part1.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(@NotNull(message = "Status is required") UserStatus status) {}
