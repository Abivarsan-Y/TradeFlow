package com.tradejapan.part4.repository;

import com.tradejapan.part4.model.Testimonial;
import com.tradejapan.part4.model.TestimonialStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    List<Testimonial> findAllByOrderByCreatedAtDesc();
    List<Testimonial> findByStatusOrderByCreatedAtDesc(TestimonialStatus status);
    List<Testimonial> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(TestimonialStatus status);
    long countByUserIdAndStatus(Long userId, TestimonialStatus status);
    long countByReviewedBy(Long reviewedBy);
    List<Testimonial> findTop15ByReviewedByNotNullOrderByReviewedAtDesc();
}
