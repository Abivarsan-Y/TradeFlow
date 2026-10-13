package com.tradejapan.part2.repository;
import com.tradejapan.part2.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FeatureRepository extends JpaRepository<Feature, Integer> {}
