package com.tradeflow.inventory.service;

import com.tradeflow.inventory.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VehicleService {
    Page<Vehicle> search(String make, String model, Integer minYear, String status, Pageable pageable);
    Vehicle getById(Integer id);
    Vehicle createFromMap(Map<String, Object> payload);
    Vehicle update(Integer id, Map<String, Object> payload);
    void delete(Integer id);
    void setStatus(Integer id, String status);
    List<Vehicle> getNewArrivals();
    Vehicle addImages(Integer id, List<String> imageUrls);
    Vehicle assignFeatures(Integer id, List<Integer> featureIds);
    void toggleNewArrival(Integer id);
    void deactivate(Integer id);
    void removeImage(Integer id, Integer imageId);
    void saveToWishlist(Integer userId, Integer vehicleId);
    void removeFromWishlist(Integer userId, Integer vehicleId);
    List<Vehicle> getSavedVehicles(Integer userId);
}
