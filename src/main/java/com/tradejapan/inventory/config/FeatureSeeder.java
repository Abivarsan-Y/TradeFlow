package com.tradejapan.inventory.config;

import com.tradejapan.inventory.model.Feature;
import com.tradejapan.inventory.model.Vehicle;
import com.tradejapan.inventory.repository.FeatureRepository;
import com.tradejapan.inventory.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Makes sure the features table contains exactly the 28 catalogue options.
 * - missing options are created
 * - old options that are not in the catalogue (earlier demo data) are removed
 * - the very first time the catalogue is created, the 5 demo vehicles get sample options
 *   (never touched again afterwards, so admin changes are never overwritten)
 */
@Component
public class FeatureSeeder implements CommandLineRunner {

    private final FeatureRepository featureRepository;
    private final VehicleRepository vehicleRepository;
    private final JdbcTemplate jdbcTemplate;

    public FeatureSeeder(FeatureRepository featureRepository,
                         VehicleRepository vehicleRepository,
                         JdbcTemplate jdbcTemplate) {
        this.featureRepository = featureRepository;
        this.vehicleRepository = vehicleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Set<String> existing = new HashSet<>();
        for (Feature f : featureRepository.findAll()) {
            existing.add(f.getName());
        }

        boolean createdAny = false;
        for (String name : FeatureCatalog.NAMES) {
            if (!existing.contains(name)) {
                Feature f = new Feature();
                f.setName(name);
                featureRepository.save(f);
                createdAny = true;
            }
        }

        // remove leftovers that are not part of the catalogue
        for (Feature f : featureRepository.findAll()) {
            if (!FeatureCatalog.NAMES.contains(f.getName())) {
                jdbcTemplate.update("DELETE FROM vehicle_features WHERE feature_id = ?", f.getFeatureId());
                featureRepository.delete(f);
            }
        }

        if (createdAny) {
            assignDemoFeatures();
        }
    }

    private void assignDemoFeatures() {
        List<String> base = List.of("Airbag", "Anti-Lock Brakes", "Power Steering", "A/C",
                "Power Windows", "Power Door Locks", "Power Mirrors", "AM/FM Radio",
                "Remote Keyless Entry", "Navigation System");

        Map<String, List<String>> extras = new HashMap<>();
        extras.put("TJ00001", List.of("Alloy Wheels", "Tilt Wheel"));
        extras.put("TJ00002", List.of("Leather Seats", "Sunroof", "Back Camera", "Cruise Control", "Push Start", "Alloy Wheels"));
        extras.put("TJ00003", List.of("Power Slide Door", "Third Row Seats", "Tinted Glass", "Rear Window Wiper"));
        extras.put("TJ00004", List.of("Alloy Wheels", "Back Camera", "Cruise Control", "Leather Seats", "Power Seats", "Digital Meter", "Fog Light", "Push Start"));
        extras.put("TJ00005", List.of("Alloy Wheels", "Back Camera", "Cruise Control", "Fog Light"));

        Map<String, Feature> byName = new HashMap<>();
        for (Feature f : featureRepository.findAll()) {
            byName.put(f.getName(), f);
        }

        for (Vehicle v : vehicleRepository.findAll()) {
            List<String> demo = extras.get(v.getRefNo());
            if (demo == null || !v.getFeatures().isEmpty()) {
                continue;
            }
            for (String name : base) {
                if (byName.containsKey(name)) v.getFeatures().add(byName.get(name));
            }
            for (String name : demo) {
                if (byName.containsKey(name)) v.getFeatures().add(byName.get(name));
            }
            vehicleRepository.save(v);
        }
    }
}
