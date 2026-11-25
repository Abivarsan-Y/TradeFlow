package com.tradejapan.reporting.service;

import com.tradejapan.users.model.Role;
import com.tradejapan.users.repository.UserRepository;
import com.tradejapan.inventory.repository.VehicleRepository;

import com.tradejapan.orders.model.Inquiry;
import com.tradejapan.orders.model.InquiryStatus;
import com.tradejapan.orders.repository.InquiryRepository;
import com.tradejapan.orders.repository.PurchaseRepository;

import com.tradejapan.reporting.dto.DashboardStats;
import com.tradejapan.engagement.model.ContactStatus;
import com.tradejapan.engagement.model.TestimonialStatus;
import com.tradejapan.engagement.repository.ContactMessageRepository;
import com.tradejapan.engagement.repository.TestimonialRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the statistics shown on the Admin Dashboard.
 */
@Service
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final PurchaseRepository purchaseRepository;
    private final ContactMessageRepository contactRepository;
    private final TestimonialRepository testimonialRepository;

    public DashboardService(
            VehicleRepository vehicleRepository,
            UserRepository userRepository,
            InquiryRepository inquiryRepository,
            PurchaseRepository purchaseRepository,
            ContactMessageRepository contactRepository,
            TestimonialRepository testimonialRepository) {

        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.inquiryRepository = inquiryRepository;
        this.purchaseRepository = purchaseRepository;
        this.contactRepository = contactRepository;
        this.testimonialRepository = testimonialRepository;
    }

    public DashboardStats stats() {

        LocalDate today =
                LocalDate.now();

        LocalDateTime weekStart =
                today.minusDays(6).atStartOfDay();

        /*
         * One entry per day for the last seven days.
         * Days with no inquiries are shown as zero.
         */
        long[] perDay =
                new long[7];

        for (Inquiry inquiry :
                inquiryRepository.findByCreatedAtAfter(
                        weekStart.minusSeconds(1)
                )) {

            long index =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            weekStart.toLocalDate(),
                            inquiry.getCreatedAt().toLocalDate()
                    );

            if (index >= 0 && index < 7) {
                perDay[(int) index]++;
            }
        }

        List<DashboardStats.DayCount> days =
                new ArrayList<>();

        for (int day = 0; day < 7; day++) {

            days.add(
                    new DashboardStats.DayCount(
                            weekStart
                                    .toLocalDate()
                                    .plusDays(day)
                                    .toString(),

                            perDay[day]
                    )
            );
        }

        return new DashboardStats(
                vehicleRepository.count(),

                userRepository.countByRole(
                        Role.ROLE_CUSTOMER
                ),

                inquiryRepository.countByStatusNot(
                        InquiryStatus.CLOSED
                ),

                purchaseRepository.countByCreatedAtAfter(
                        LocalDateTime.now().minusDays(30)
                ),

                days,

                contactRepository.countByStatus(
                        ContactStatus.NEW
                ),

                testimonialRepository.countByStatus(
                        TestimonialStatus.PENDING
                ),

                testimonialRepository.countByStatus(
                        TestimonialStatus.APPROVED
                ),

                testimonialRepository.countByStatus(
                        TestimonialStatus.REJECTED
                )
        );
    }
}