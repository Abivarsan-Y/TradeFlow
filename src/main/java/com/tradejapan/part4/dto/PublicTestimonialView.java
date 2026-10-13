package com.tradejapan.part4.dto;

import java.time.LocalDateTime;

/** What the public home page may see: no user id, no status, no admin names. */
public record PublicTestimonialView(String name, String country, Integer rating, String message, LocalDateTime createdAt) {}
