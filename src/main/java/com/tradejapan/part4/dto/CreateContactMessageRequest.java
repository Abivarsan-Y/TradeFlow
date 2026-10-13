package com.tradejapan.part4.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContactMessageRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be 100 characters or less") String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 150, message = "Email must be 150 characters or less") String email,
        @NotBlank(message = "Subject is required")
        @Size(max = 200, message = "Subject must be 200 characters or less") String subject,
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message must be 2000 characters or less") String message
) {}
