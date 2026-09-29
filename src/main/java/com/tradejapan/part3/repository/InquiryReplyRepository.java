package com.tradejapan.part3.repository;

import com.tradejapan.part3.model.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {
    List<InquiryReply> findByInquiryIdOrderByCreatedAtAsc(Long inquiryId);
    void deleteByInquiryId(Long inquiryId);
}
