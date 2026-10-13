package com.tradejapan.part2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The wishlist ("Saved Vehicles"): one row per (user, vehicle) in the saved_vehicles table.
 * The table has no entity of its own (it is a pure junction table), so plain SQL is used.
 */
@Repository
public class SavedVehicleRepository {

    private final JdbcTemplate jdbc;

    public SavedVehicleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Saving the same vehicle twice is harmless (INSERT IGNORE keeps one row). */
    public void save(Long userId, Integer vehicleId) {
        jdbc.update("INSERT IGNORE INTO saved_vehicles (user_id, vehicle_id) VALUES (?, ?)", userId, vehicleId);
    }

    public void delete(Long userId, Integer vehicleId) {
        jdbc.update("DELETE FROM saved_vehicles WHERE user_id = ? AND vehicle_id = ?", userId, vehicleId);
    }

    /** Newest saved first. */
    public List<Integer> findVehicleIds(Long userId) {
        return jdbc.queryForList(
                "SELECT vehicle_id FROM saved_vehicles WHERE user_id = ? ORDER BY saved_at DESC, vehicle_id DESC",
                Integer.class, userId);
    }
}
