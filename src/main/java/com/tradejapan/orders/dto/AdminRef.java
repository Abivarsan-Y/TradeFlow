package com.tradejapan.orders.dto;

/** A small reference to an admin (id + display name), used for the "Admin" filter. */
public record AdminRef(Long id, String name) {}
