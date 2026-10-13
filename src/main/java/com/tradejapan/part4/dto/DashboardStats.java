package com.tradejapan.part4.dto;

import java.util.List;

public record DashboardStats(
        long totalVehicles,
        long totalCustomers,
        long openInquiries,
        long purchasesLast30Days,
        List<DayCount> inquiriesLast7Days,
        long newContactMessages,
        long pendingTestimonials,
        long approvedTestimonials,
        long rejectedTestimonials
) {
    public record DayCount(String date, long count) {}
}
