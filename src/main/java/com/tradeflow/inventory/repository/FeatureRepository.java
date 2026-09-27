package com.tradeflow.inventory.repository;

import com.tradeflow.inventory.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<Feature, Integer> {
}
