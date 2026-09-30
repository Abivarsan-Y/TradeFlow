package com.tradejapan.inventory.controller;

import com.tradejapan.inventory.dto.StatusUpdateRequest;
import com.tradejapan.inventory.model.Feature;
import com.tradejapan.inventory.model.Vehicle;
import com.tradejapan.inventory.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // ── Public Endpoints ─────────────────────────────────────
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

    /** The 28 option names, in display order (public: the vehicle page shows them as ON / OFF). */
    @GetMapping("/features")
    public List<Feature> getFeatures() {
        return vehicleService.getAllFeatures();
    }

    // ── Admin Endpoints (secured by SecurityConfig) ──────────
    @PostMapping("/admin/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@RequestBody Map<String, Object> payload) {
        return vehicleService.createFromMap(payload);
    }

    @PutMapping("/admin/vehicles/{id}")
    public Vehicle update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return vehicleService.update(id, payload);
    }

    @PatchMapping("/admin/vehicles/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusUpdateRequest request) {
        vehicleService.setStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
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

    @PatchMapping("/admin/vehicles/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Integer id) {
        vehicleService.setActive(id, true);
        return ResponseEntity.ok().build();
    }

    /** All vehicles including deactivated ones, for the admin panel. */
    @GetMapping("/admin/vehicles")
    public List<Vehicle> adminList() {
        return vehicleService.getAllForAdmin();
    }

    @DeleteMapping("/admin/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        vehicleService.delete(id);
    }

    @PostMapping("/admin/vehicles/{id}/images")
    public ResponseEntity<Vehicle> uploadImages(@PathVariable Integer id,
                                                 @RequestParam("files") org.springframework.web.multipart.MultipartFile[] files) {
        List<String> urls = new java.util.ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            urls.add("https://picsum.photos/id/" + (100 + i) + "/800/500");
        }
        return ResponseEntity.ok(vehicleService.addImages(id, urls));
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
