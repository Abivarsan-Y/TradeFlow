package com.tradeflow.inventory.repository;

import com.tradeflow.inventory.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Integer> {
}
