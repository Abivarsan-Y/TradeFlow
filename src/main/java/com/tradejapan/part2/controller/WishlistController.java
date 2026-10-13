package com.tradejapan.inventory.controller;

import com.tradejapan.inventory.model.Vehicle;
import com.tradejapan.inventory.service.VehicleService;
import com.tradejapan.part3.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The logged-in customer's wishlist ("Saved Vehicles").
 * The user always comes from the login token, never from the browser, so nobody can read or change someone else's list.
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final VehicleService vehicleService;
    private final CurrentUserService currentUserService;

    public WishlistController(VehicleService vehicleService, CurrentUserService currentUserService) {
        this.vehicleService = vehicleService;
        this.currentUserService = currentUserService;
    }

    /** The saved vehicles themselves (for the Wishlist tab on the Account page). */
    @GetMapping
    public List<Vehicle> getSaved() {
        return vehicleService.getSavedVehicles(currentUserService.requireUser().getId());
    }

    /** Only the ids (so a vehicle page can show whether the heart is already filled). */
    @GetMapping("/ids")
    public List<Integer> getSavedIds() {
        List<Integer> ids = new ArrayList<>();
        for (Vehicle v : vehicleService.getSavedVehicles(currentUserService.requireUser().getId())) {
            ids.add(v.getVehicleId());
        }
        return ids;
    }

    @PostMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@PathVariable Integer vehicleId) {
        vehicleService.saveToWishlist(currentUserService.requireUser().getId(), vehicleId);
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> remove(@PathVariable Integer vehicleId) {
        vehicleService.removeFromWishlist(currentUserService.requireUser().getId(), vehicleId);
        return ResponseEntity.noContent().build();
    }
}
