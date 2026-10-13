package com.tradejapan.part4.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTestimonialRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be 100 characters or less") String name,
        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must be 100 characters or less") String country,
        @NotNull(message = "Please select a rating")
        @Min(value = 1, message = "Rating must be between 1 and 5")
        @Max(value = 5, message = "Rating must be between 1 and 5") Integer rating,
        @NotBlank(message = "Testimonial is required")
        @Size(min = 10, max = 1000, message = "Testimonial must be between 10 and 1000 characters") String message
) {}
