package com.tradejapan.reporting.controller;

import com.tradejapan.reporting.dto.AdminActivityReport;
import com.tradejapan.reporting.dto.SalesSummary;
import com.tradejapan.reporting.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/sales-summary")
    public SalesSummary salesSummary() {
        return service.salesSummary();
    }

    @GetMapping("/admin-activity")
    public AdminActivityReport adminActivity() {
        return service.adminActivity();
    }
}