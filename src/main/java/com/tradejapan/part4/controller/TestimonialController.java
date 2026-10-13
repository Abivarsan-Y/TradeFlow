package com.tradejapan.part4.controller;

import com.tradejapan.part4.dto.CreateTestimonialRequest;
import com.tradejapan.part4.dto.PublicTestimonialView;
import com.tradejapan.part4.dto.TestimonialView;
import com.tradejapan.part4.model.Testimonial;
import com.tradejapan.part4.service.AdminNames;
import com.tradejapan.part4.service.TestimonialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TestimonialController {

    private final TestimonialService testimonialService;
    private final AdminNames adminNames;

    public TestimonialController(
            TestimonialService testimonialService,
            AdminNames adminNames) {

        this.testimonialService = testimonialService;
        this.adminNames = adminNames;
    }

    @PostMapping("/testimonials")
    public ResponseEntity<TestimonialView> submitTestimonial(
            @Valid @RequestBody CreateTestimonialRequest request) {

        Long customerId =
                adminNames.currentCustomerId();

        Testimonial testimonial =
                testimonialService.submitTestimonial(
                        request,
                        customerId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toAdminView(testimonial));
    }

    @GetMapping("/testimonials/approved")
    public List<PublicTestimonialView> getApprovedTestimonials() {

        return testimonialService
                .getApprovedTestimonials()
                .stream()
                .map(this::toPublicView)
                .toList();
    }

    @GetMapping("/testimonials/me")
    public List<TestimonialView> getMyTestimonials() {

        Long customerId =
                adminNames.currentCustomerId();

        return testimonialService
                .getTestimonialsByUser(customerId)
                .stream()
                .map(this::toAdminView)
                .toList();
    }

    @GetMapping("/admin/testimonials")
    public List<TestimonialView> getAllTestimonials() {

        return testimonialService
                .getAllTestimonials()
                .stream()
                .map(this::toAdminView)
                .toList();
    }

    @PatchMapping("/admin/testimonials/{id}/approve")
    public TestimonialView approveTestimonial(
            @PathVariable Long id) {

        Long adminId =
                adminNames.currentAdminId();

        Testimonial testimonial =
                testimonialService.approveTestimonial(
                        id,
                        adminId
                );

        return toAdminView(testimonial);
    }

    @PatchMapping("/admin/testimonials/{id}/reject")
    public TestimonialView rejectTestimonial(
            @PathVariable Long id) {

        Long adminId =
                adminNames.currentAdminId();

        Testimonial testimonial =
                testimonialService.rejectTestimonial(
                        id,
                        adminId
                );

        return toAdminView(testimonial);
    }

    @DeleteMapping("/admin/testimonials/{id}")
    public ResponseEntity<Void> deleteTestimonial(
            @PathVariable Long id) {

        testimonialService.deleteTestimonial(id);

        return ResponseEntity.noContent().build();
    }

    private PublicTestimonialView toPublicView(
            Testimonial testimonial) {

        return new PublicTestimonialView(
                testimonial.getName(),
                testimonial.getCountry(),
                testimonial.getRating(),
                testimonial.getMessage(),
                testimonial.getCreatedAt()
        );
    }

    private TestimonialView toAdminView(
            Testimonial testimonial) {

        return new TestimonialView(
                testimonial.getTestimonialId(),
                testimonial.getName(),
                testimonial.getCountry(),
                testimonial.getRating(),
                testimonial.getMessage(),
                testimonial.getStatus(),
                testimonial.getCreatedAt(),
                adminNames.nameOf(
                        testimonial.getReviewedBy()
                ),
                testimonial.getReviewedAt()
        );
    }
}