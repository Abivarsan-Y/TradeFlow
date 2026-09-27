package com.tradeflow.inventory.repository;

import com.tradeflow.inventory.model.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleImageRepository extends JpaRepository<VehicleImage, Integer> {
}
