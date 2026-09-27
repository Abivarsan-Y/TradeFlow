package com.tradeflow.inventory.repository;

import com.tradeflow.inventory.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findTop8ByOrderByCreatedAtDesc();
}
