package com.tradejapan.cms.dto;

import com.tradejapan.part3.dto.InquiryView;
import com.tradejapan.part3.dto.PurchaseView;

import java.util.List;

/** Read-only detail of one customer: profile plus their inquiries and purchases. */
public record CustomerDetailView(CustomerView profile, List<InquiryView> inquiries, List<PurchaseView> purchases) {}
