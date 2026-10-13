package com.tradejapan.part3.repository;

import com.tradejapan.part3.model.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {
    List<InquiryReply> findByInquiryIdOrderByCreatedAtAsc(Long inquiryId);
    void deleteByInquiryId(Long inquiryId);
    long countByInquiryId(Long inquiryId);
    List<InquiryReply> findByInquiryIdIn(java.util.Collection<Long> inquiryIds);
    long countByUserIdAndFromAdminTrue(Long userId);
    List<InquiryReply> findTop50ByFromAdminTrueOrderByCreatedAtDesc();
}
