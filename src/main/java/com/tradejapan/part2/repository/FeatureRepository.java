package com.tradejapan.inventory.repository;
import com.tradejapan.inventory.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FeatureRepository extends JpaRepository<Feature, Integer> {}
