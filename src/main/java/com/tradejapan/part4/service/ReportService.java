package com.tradejapan.part4.service;

import com.tradejapan.part1.model.Role;
import com.tradejapan.part1.model.User;
import com.tradejapan.part1.repository.UserRepository;

import com.tradejapan.part3.dto.PurchaseView;
import com.tradejapan.part3.model.InquiryReply;
import com.tradejapan.part3.model.Purchase;
import com.tradejapan.part3.model.PurchaseStatus;
import com.tradejapan.part3.repository.InquiryReplyRepository;
import com.tradejapan.part3.repository.PurchaseRepository;
import com.tradejapan.part3.service.ViewMapper;

import com.tradejapan.part4.dto.AdminActivityReport;
import com.tradejapan.part4.dto.SalesSummary;
import com.tradejapan.part4.model.ContactMessage;
import com.tradejapan.part4.model.Testimonial;
import com.tradejapan.part4.repository.ContactMessageRepository;
import com.tradejapan.part4.repository.TestimonialRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Super Admin reports:
 * 1. Sales Summary
 * 2. Admin Activity
 */
@Service
public class ReportService {

    private final PurchaseRepository purchaseRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final ContactMessageRepository contactRepository;
    private final TestimonialRepository testimonialRepository;
    private final ViewMapper viewMapper;
    private final AdminNames adminNames;

    public ReportService(
            PurchaseRepository purchaseRepository,
            InquiryReplyRepository replyRepository,
            UserRepository userRepository,
            ContactMessageRepository contactRepository,
            TestimonialRepository testimonialRepository,
            ViewMapper viewMapper,
            AdminNames adminNames) {

        this.purchaseRepository = purchaseRepository;
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.testimonialRepository = testimonialRepository;
        this.viewMapper = viewMapper;
        this.adminNames = adminNames;
    }

    // =========================================================
    // SALES SUMMARY
    // =========================================================

    public SalesSummary salesSummary() {

        List<PurchaseView> purchases =
                viewMapper.purchases(
                        purchaseRepository.findAll()
                );

        BigDecimal total =
                BigDecimal.ZERO;

        BigDecimal collected =
                BigDecimal.ZERO;

        Map<String, Long> byStatus =
                new LinkedHashMap<>();

        for (PurchaseStatus status :
                PurchaseStatus.values()) {

            byStatus.put(
                    status.name(),
                    0L
            );
        }

        Map<String, BigDecimal> destinationValue =
                new HashMap<>();

        Map<String, Long> destinationCount =
                new HashMap<>();

        Map<String, BigDecimal> makeValue =
                new HashMap<>();

        Map<String, Long> makeCount =
                new HashMap<>();

        Map<YearMonth, BigDecimal> monthValue =
                new HashMap<>();

        Map<YearMonth, Long> monthCount =
                new HashMap<>();

        for (PurchaseView purchase : purchases) {

            BigDecimal totalPrice =
                    purchase.totalPrice() == null
                            ? BigDecimal.ZERO
                            : purchase.totalPrice();

            BigDecimal advancePaid =
                    purchase.advancePaid() == null
                            ? BigDecimal.ZERO
                            : purchase.advancePaid();

            total =
                    total.add(totalPrice);

            collected =
                    collected.add(advancePaid);

            if (purchase.status() != null) {

                byStatus.merge(
                        purchase.status().name(),
                        1L,
                        Long::sum
                );
            }

            String destination =
                    purchase.destinationCountry() == null
                            || purchase.destinationCountry().isBlank()
                            ? "Unknown"
                            : purchase.destinationCountry();

            String make =
                    purchase.vehicleMake() == null
                            || purchase.vehicleMake().isBlank()
                            ? "Unknown"
                            : purchase.vehicleMake();

            destinationValue.merge(
                    destination,
                    totalPrice,
                    BigDecimal::add
            );

            destinationCount.merge(
                    destination,
                    1L,
                    Long::sum
            );

            makeValue.merge(
                    make,
                    totalPrice,
                    BigDecimal::add
            );

            makeCount.merge(
                    make,
                    1L,
                    Long::sum
            );

            if (purchase.createdAt() != null) {

                YearMonth yearMonth =
                        YearMonth.from(
                                purchase.createdAt()
                        );

                monthValue.merge(
                        yearMonth,
                        totalPrice,
                        BigDecimal::add
                );

                monthCount.merge(
                        yearMonth,
                        1L,
                        Long::sum
                );
            }
        }

        List<SalesSummary.Row> months =
                new ArrayList<>();

        YearMonth currentMonth =
                YearMonth.now();

        // Last six months, oldest first
        for (int i = 5; i >= 0; i--) {

            YearMonth month =
                    currentMonth.minusMonths(i);

            months.add(
                    new SalesSummary.Row(
                            month.toString(),
                            monthCount.getOrDefault(
                                    month,
                                    0L
                            ),
                            monthValue.getOrDefault(
                                    month,
                                    BigDecimal.ZERO
                            )
                    )
            );
        }

        BigDecimal balanceDue =
                total.subtract(collected);

        return new SalesSummary(
                "USD",
                purchases.size(),
                total,
                collected,
                balanceDue,
                byStatus,
                top5(
                        destinationValue,
                        destinationCount
                ),
                top5(
                        makeValue,
                        makeCount
                ),
                months
        );
    }

    /**
     * Return top five rows ordered by value.
     */
    private List<SalesSummary.Row> top5(
            Map<String, BigDecimal> values,
            Map<String, Long> counts) {

        List<SalesSummary.Row> rows =
                new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry :
                values.entrySet()) {

            rows.add(
                    new SalesSummary.Row(
                            entry.getKey(),
                            counts.getOrDefault(
                                    entry.getKey(),
                                    0L
                            ),
                            entry.getValue()
                    )
            );
        }

        rows.sort(
                Comparator.comparing(
                        SalesSummary.Row::value
                ).reversed()
        );

        if (rows.size() > 5) {

            return new ArrayList<>(
                    rows.subList(
                            0,
                            5
                    )
            );
        }

        return rows;
    }

    // =========================================================
    // ADMIN ACTIVITY REPORT
    // =========================================================

    public AdminActivityReport adminActivity() {

        List<User> admins =
                new ArrayList<>(
                        userRepository.findByRole(
                                Role.ROLE_SUPER_ADMIN
                        )
                );

        admins.addAll(
                userRepository.findByRole(
                        Role.ROLE_ADMIN
                )
        );

        List<AdminActivityReport.AdminRow> rows =
                new ArrayList<>();

        for (User admin : admins) {

            rows.add(
                    new AdminActivityReport.AdminRow(
                            admin.getId(),
                            AdminNames.fullName(admin),
                            admin.getEmail(),

                            admin.getRole()
                                    == Role.ROLE_SUPER_ADMIN
                                    ? "Super Admin"
                                    : "Admin",

                            replyRepository
                                    .countByUserIdAndFromAdminTrue(
                                            admin.getId()
                                    ),

                            purchaseRepository
                                    .countByProcessedBy(
                                            admin.getId()
                                    ),

                            testimonialRepository
                                    .countByReviewedBy(
                                            admin.getId()
                                    ),

                            contactRepository
                                    .countByHandledBy(
                                            admin.getId()
                                    )
                    )
            );
        }

        // Latest admin actions

        List<InquiryReply> replies =
                replyRepository
                        .findTop50ByFromAdminTrueOrderByCreatedAtDesc();

        List<Purchase> purchases =
                purchaseRepository
                        .findTop15ByOrderByCreatedAtDesc();

        List<Testimonial> testimonials =
                testimonialRepository
                        .findTop15ByReviewedByNotNullOrderByReviewedAtDesc();

        List<ContactMessage> contacts =
                contactRepository
                        .findTop15ByHandledByNotNullOrderByHandledAtDesc();

        List<Long> adminIds =
                new ArrayList<>();

        replies.forEach(reply ->
                adminIds.add(
                        reply.getUserId()
                )
        );

        purchases.forEach(purchase ->
                adminIds.add(
                        purchase.getProcessedBy()
                )
        );

        testimonials.forEach(testimonial ->
                adminIds.add(
                        testimonial.getReviewedBy()
                )
        );

        contacts.forEach(contact ->
                adminIds.add(
                        contact.getHandledBy()
                )
        );

        Map<Long, String> names =
                adminNames.byId(adminIds);

        List<AdminActivityReport.Event> events =
                new ArrayList<>();

        for (InquiryReply reply : replies) {

            events.add(
                    new AdminActivityReport.Event(
                            "Reply",

                            "Replied to inquiry #"
                                    + reply.getInquiryId(),

                            names.getOrDefault(
                                    reply.getUserId(),
                                    "Unknown"
                            ),

                            reply.getCreatedAt()
                    )
            );
        }

        for (Purchase purchase : purchases) {

            events.add(
                    new AdminActivityReport.Event(
                            "Purchase",

                            "Created purchase #"
                                    + purchase.getPurchaseId(),

                            names.getOrDefault(
                                    purchase.getProcessedBy(),
                                    "Unknown"
                            ),

                            purchase.getCreatedAt()
                    )
            );
        }

        for (Testimonial testimonial :
                testimonials) {

            String status =
                    testimonial
                            .getStatus()
                            .name()
                            .toLowerCase();

            status =
                    Character.toUpperCase(
                            status.charAt(0)
                    )
                            + status.substring(1);

            events.add(
                    new AdminActivityReport.Event(
                            "Testimonial",

                            status
                                    + " the testimonial from "
                                    + testimonial.getName(),

                            names.getOrDefault(
                                    testimonial.getReviewedBy(),
                                    "Unknown"
                            ),

                            testimonial.getReviewedAt()
                    )
            );
        }

        for (ContactMessage contact :
                contacts) {

            events.add(
                    new AdminActivityReport.Event(
                            "Contact message",

                            "Marked the message from "
                                    + contact.getName()
                                    + " as "
                                    + contact
                                    .getStatus()
                                    .name()
                                    .toLowerCase(),

                            names.getOrDefault(
                                    contact.getHandledBy(),
                                    "Unknown"
                            ),

                            contact.getHandledAt()
                    )
            );
        }

        events.sort(
                Comparator.comparing(
                        AdminActivityReport.Event::time
                ).reversed()
        );

        List<AdminActivityReport.Event> recent;

        if (events.size() > 15) {

            recent =
                    new ArrayList<>(
                            events.subList(
                                    0,
                                    15
                            )
                    );

        } else {

            recent = events;
        }

        return new AdminActivityReport(
                rows,
                recent
        );
    }
}