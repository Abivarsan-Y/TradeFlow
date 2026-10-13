package com.tradejapan.part2.repository;
import com.tradejapan.part2.model.Vehicle;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer>, JpaSpecificationExecutor<Vehicle> {
    List<Vehicle> findTop8ByOrderByCreatedAtDesc();
    @Query("select v from Vehicle v where v.isNewArrival = true and v.isActive = true order by v.createdAt desc")
    List<Vehicle> findNewArrivals(org.springframework.data.domain.Pageable pageable);
    Optional<Vehicle> findByRefNo(String refNo);
}
