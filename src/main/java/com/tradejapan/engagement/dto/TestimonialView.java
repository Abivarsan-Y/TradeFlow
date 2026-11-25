package com.tradejapan.engagement.dto;

import com.tradejapan.engagement.model.TestimonialStatus;

import java.time.LocalDateTime;

/** Full view for admins and for the customer's own list. */
public record TestimonialView(
        Long testimonialId,
        String name,
        String country,
        Integer rating,
        String message,
        TestimonialStatus status,
        LocalDateTime createdAt,
        String reviewedByName,
        LocalDateTime reviewedAt
) {}
