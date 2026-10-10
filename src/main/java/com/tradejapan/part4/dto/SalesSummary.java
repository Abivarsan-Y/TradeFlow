package com.tradejapan.part4.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SalesSummary(
        String currency,
        long totalPurchases,
        BigDecimal totalSalesValue,
        BigDecimal totalCollected,
        BigDecimal balanceDue,
        Map<String, Long> byStatus,
        List<Row> topDestinations,
        List<Row> topMakes,
        List<Row> monthly
) {
    /** label = country / make / month; count = number of purchases; value = total price. */
    public record Row(String label, long count, BigDecimal value) {}
}
