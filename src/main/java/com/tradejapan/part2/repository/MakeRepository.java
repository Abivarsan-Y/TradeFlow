package com.tradeflow.inventory.repository;

import com.tradeflow.inventory.model.Make;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MakeRepository extends JpaRepository<Make, Integer> {
}
