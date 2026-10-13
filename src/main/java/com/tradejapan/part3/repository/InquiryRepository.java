package com.tradejapan.part3.repository;

import com.tradejapan.part3.model.Inquiry;
import com.tradejapan.part3.model.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    long countByStatusNot(InquiryStatus status);
    /** [userId, number of inquiries] for every user who has at least one (used by Manage Customers). */
    @Query("select i.userId, count(i) from Inquiry i group by i.userId")
    List<Object[]> countPerUser();
    List<Inquiry> findByCreatedAtAfter(java.time.LocalDateTime time);
}
