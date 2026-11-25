package com.tradejapan.users.dto;

import com.tradejapan.orders.dto.InquiryView;
import com.tradejapan.orders.dto.PurchaseView;

import java.util.List;

/** Read-only detail of one customer: profile plus their inquiries and purchases. */
public record CustomerDetailView(CustomerView profile, List<InquiryView> inquiries, List<PurchaseView> purchases) {}
