package com.tradejapan.part4.service;

import com.tradejapan.part4.dto.CreateTestimonialRequest;
import com.tradejapan.part4.model.Testimonial;
import com.tradejapan.part4.model.TestimonialStatus;
import com.tradejapan.part4.repository.TestimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    public TestimonialService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    /**
     * Submit a testimonial.
     *
     * userId can temporarily be null until the shared
     * authentication/JWT module is integrated.
     */
    @Transactional
    public Testimonial submitTestimonial(
            CreateTestimonialRequest request,
            Long userId) {

        Testimonial testimonial = new Testimonial();

        testimonial.setUserId(userId);
        testimonial.setName(request.name().trim());
        testimonial.setCountry(request.country().trim());
        testimonial.setRating(request.rating());
        testimonial.setMessage(request.message().trim());

        // Every new testimonial must wait for admin approval.
        testimonial.setStatus(TestimonialStatus.PENDING);

        return testimonialRepository.save(testimonial);
    }

    /**
     * Temporary overload while authentication integration
     * is still being completed by the group.
     */
    @Transactional
    public Testimonial submitTestimonial(
            CreateTestimonialRequest request) {

        return submitTestimonial(request, null);
    }

    /**
     * Public website:
     * only approved testimonials are returned.
     */
    @Transactional(readOnly = true)
    public List<Testimonial> getApprovedTestimonials() {

        return testimonialRepository
                .findByStatusOrderByCreatedAtDesc(
                        TestimonialStatus.APPROVED
                );
    }

    /**
     * Admin dashboard:
     * retrieve all testimonials, newest first.
     */
    @Transactional(readOnly = true)
    public List<Testimonial> getAllTestimonials() {

        return testimonialRepository
                .findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieve testimonials submitted by one customer.
     */
    @Transactional(readOnly = true)
    public List<Testimonial> getTestimonialsByUser(Long userId) {

        return testimonialRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Admin approves a pending testimonial.
     *
     * adminId will later come from the authenticated
     * admin/JWT information.
     */
    @Transactional
    public Testimonial approveTestimonial(
            Long testimonialId,
            Long adminId) {

        Testimonial testimonial =
                findTestimonialOrThrow(testimonialId);

        testimonial.setStatus(TestimonialStatus.APPROVED);
        testimonial.setReviewedBy(adminId);
        testimonial.setReviewedAt(LocalDateTime.now());

        return testimonialRepository.save(testimonial);
    }

    /**
     * Admin rejects a testimonial.
     */
    @Transactional
    public Testimonial rejectTestimonial(
            Long testimonialId,
            Long adminId) {

        Testimonial testimonial =
                findTestimonialOrThrow(testimonialId);

        testimonial.setStatus(TestimonialStatus.REJECTED);
        testimonial.setReviewedBy(adminId);
        testimonial.setReviewedAt(LocalDateTime.now());

        return testimonialRepository.save(testimonial);
    }

    /**
     * Delete a testimonial.
     */
    @Transactional
    public void deleteTestimonial(Long testimonialId) {

        Testimonial testimonial =
                findTestimonialOrThrow(testimonialId);

        testimonialRepository.delete(testimonial);
    }

    /**
     * Number of testimonials waiting for admin review.
     */
    @Transactional(readOnly = true)
    public long countPendingTestimonials() {

        return testimonialRepository
                .countByStatus(TestimonialStatus.PENDING);
    }

    /**
     * Internal helper used by approve/reject/delete.
     */
    private Testimonial findTestimonialOrThrow(
            Long testimonialId) {

        return testimonialRepository
                .findById(testimonialId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Testimonial not found: "
                                        + testimonialId
                        )
                );
    }
}