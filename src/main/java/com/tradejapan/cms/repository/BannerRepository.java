package com.tradejapan.cms.repository;

import com.tradejapan.cms.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    // Used by old code — keep for compatibility
    List<Banner> findByActiveTrueOrderBySortOrderAsc();

    // Grouped queries
    List<Banner> findByActiveTrueAndSectionNumIsNullOrderBySortOrderAsc();
    List<Banner> findByActiveTrueAndSectionNumOrderBySortOrderAsc(Integer sectionNum);

    // Count queries for limit enforcement
    long countByActiveTrueAndSectionNumIsNull();
    long countByActiveTrueAndSectionNum(Integer sectionNum);
}