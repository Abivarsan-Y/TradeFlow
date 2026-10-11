package com.tradejapan.part4.controller;

import com.tradejapan.part4.dto.CreateTestimonialRequest;
import com.tradejapan.part4.dto.PublicTestimonialView;
import com.tradejapan.part4.dto.TestimonialView;
import com.tradejapan.part4.model.Testimonial;
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

    public TestimonialController(TestimonialService testimonialService) {
        this.testimonialService = testimonialService;
    }

    /*
     * CUSTOMER
     * Submit a new testimonial.
     *
     * Authentication will later provide the actual customer ID.
     */
    @PostMapping("/testimonials")
    public ResponseEntity<TestimonialView> submitTestimonial(
            @Valid @RequestBody CreateTestimonialRequest request) {

        Testimonial testimonial =
                testimonialService.submitTestimonial(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toAdminView(testimonial));
    }

    /*
     * PUBLIC
     * Only APPROVED testimonials are displayed.
     */
    @GetMapping("/testimonials/approved")
    public List<PublicTestimonialView> getApprovedTestimonials() {

        return testimonialService
                .getApprovedTestimonials()
                .stream()
                .map(this::toPublicView)
                .toList();
    }

    /*
     * ADMIN
     * View every submitted testimonial.
     */
    @GetMapping("/admin/testimonials")
    public List<TestimonialView> getAllTestimonials() {

        return testimonialService
                .getAllTestimonials()
                .stream()
                .map(this::toAdminView)
                .toList();
    }

    /*
     * ADMIN
     * Approve a testimonial.
     *
     * adminId will later come from JWT authentication.
     */
    @PatchMapping("/admin/testimonials/{id}/approve")
    public TestimonialView approveTestimonial(
            @PathVariable Long id) {

        Testimonial testimonial =
                testimonialService.approveTestimonial(
                        id,
                        null
                );

        return toAdminView(testimonial);
    }

    /*
     * ADMIN
     * Reject a testimonial.
     */
    @PatchMapping("/admin/testimonials/{id}/reject")
    public TestimonialView rejectTestimonial(
            @PathVariable Long id) {

        Testimonial testimonial =
                testimonialService.rejectTestimonial(
                        id,
                        null
                );

        return toAdminView(testimonial);
    }

    /*
     * ADMIN
     * Delete testimonial.
     */
    @DeleteMapping("/admin/testimonials/{id}")
    public ResponseEntity<Void> deleteTestimonial(
            @PathVariable Long id) {

        testimonialService.deleteTestimonial(id);

        return ResponseEntity.noContent().build();
    }

    /*
     * Converts database entity into safe public response.
     */
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

    /*
     * Converts database entity into admin response.
     *
     * reviewedByName remains null until the shared
     * authentication/User module is integrated.
     */
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
                null,
                testimonial.getReviewedAt()
        );
    }
}