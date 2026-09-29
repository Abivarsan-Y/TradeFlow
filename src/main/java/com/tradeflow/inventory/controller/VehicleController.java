package com.tradeflow.inventory.controller;

import com.tradeflow.inventory.model.Vehicle;
import com.tradeflow.inventory.service.VehicleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // Public endpoints
    @GetMapping("/vehicles")
    public List<Vehicle> search(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return vehicleService.search(make, model, minYear, status, PageRequest.of(page, size)).getContent();
    }

    @GetMapping("/vehicles/{id}")
    public Vehicle getById(@PathVariable Integer id) {
        return vehicleService.getById(id);
    }

    @GetMapping("/vehicles/new-arrivals")
    public List<Vehicle> getNewArrivals() {
        return vehicleService.getNewArrivals();
    }

    // Customer wishlist endpoints
    @PostMapping("/vehicles/{id}/save")
    public ResponseEntity<Void> saveToWishlist(@PathVariable Integer id) {
        // Hardcoded user 2 for testing
        vehicleService.saveToWishlist(2, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vehicles/{id}/save")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Integer id) {
        vehicleService.removeFromWishlist(2, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/me/saved-vehicles")
    public List<Vehicle> viewWishlist() {
        return vehicleService.getSavedVehicles(2); // Hardcoded user 2
    }

    // Admin endpoints
    @PostMapping("/admin/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@RequestBody Map<String, Object> payload) {
        return vehicleService.createFromMap(payload);
    }

    @PutMapping("/admin/vehicles/{id}")
    public Vehicle update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return vehicleService.update(id, payload);
    }

    @PatchMapping("/admin/vehicles/{id}/new-arrival")
    public ResponseEntity<Void> toggleNewArrival(@PathVariable Integer id) {
        vehicleService.toggleNewArrival(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/vehicles/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        vehicleService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        vehicleService.delete(id);
    }

    // Image endpoints
    @PostMapping("/admin/vehicles/{id}/images")
    public ResponseEntity<Vehicle> uploadImages(@PathVariable Integer id, @RequestParam("files") MultipartFile[] files) {
        // Simplistic file handling for demo
        List<String> urls = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            urls.add("https://picsum.photos/id/" + (Math.random() * 1000) + "/800/500");
        }
        Vehicle updated = vehicleService.addImages(id, urls);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/admin/vehicles/{id}/images/{imageId}")
    public ResponseEntity<Void> removeImage(@PathVariable Integer id, @PathVariable Integer imageId) {
        vehicleService.removeImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/vehicles/{id}/features")
    public Vehicle assignFeatures(@PathVariable Integer id, @RequestBody List<Integer> featureIds) {
        return vehicleService.assignFeatures(id, featureIds);
    }
}
