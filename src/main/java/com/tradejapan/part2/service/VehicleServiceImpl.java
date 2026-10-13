package com.tradejapan.part2.service;

import com.tradejapan.part1.repository.UserRepository;
import com.tradejapan.part2.config.FeatureCatalog;
import com.tradejapan.part2.model.*;
import com.tradejapan.part2.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    /** Every one of these must be filled when an admin saves a vehicle (key in the JSON payload, label for the error message). */
    private static final String[][] REQUIRED_FIELDS = {
            {"make", "Make"}, {"model", "Model"}, {"manufactureYear", "Manufacture Year"},
            {"fobPrice", "FOB Price"}, {"condition", "Condition"}, {"description", "Overview"},
            // the 18 specifications
            {"refNo", "Ref No"}, {"category", "Body Type"}, {"vin", "Chassis No"},
            {"modelCode", "Model Code"}, {"grade", "Grade"}, {"engineType", "Engine Model"},
            {"mileage", "Odometer"}, {"displacementCc", "Displacement"}, {"steering", "Steering"},
            {"transmission", "Transmission"}, {"doors", "Doors"}, {"passengers", "Passengers"},
            {"dimension", "Dimension"}, {"size", "Size"}, {"exteriorColor", "Exterior Color"},
            {"driveType", "Drive Type"}, {"fuelType", "Fuel Type"}, {"inventoryLocation", "Location"}
    };

    private static final List<String> STATUSES = List.of("AVAILABLE", "RESERVED", "SOLD");
    private static final List<String> CONDITIONS = List.of("NEW", "USED");

    private final VehicleRepository vehicleRepository;
    private final ModelRepository modelRepository;
    private final MakeRepository makeRepository;
    private final FeatureRepository featureRepository;
    private final VehicleImageRepository imageRepository;
    // Uses Rahman's UserRepository so wishlist links to the real User table
    private final UserRepository userRepository;
    private final SavedVehicleRepository savedVehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, ModelRepository modelRepository,
                               MakeRepository makeRepository, FeatureRepository featureRepository,
                               VehicleImageRepository imageRepository, UserRepository userRepository,
                               SavedVehicleRepository savedVehicleRepository) {
        this.vehicleRepository = vehicleRepository;
        this.modelRepository = modelRepository;
        this.makeRepository = makeRepository;
        this.featureRepository = featureRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
        this.savedVehicleRepository = savedVehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehicle> search(String make, String model, Integer minYear, String status, Pageable pageable) {
        Specification<Vehicle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            if (make != null && !make.trim().isEmpty())
                predicates.add(cb.equal(root.join("model").join("make").get("name"), make));
            if (model != null && !model.trim().isEmpty())
                predicates.add(cb.equal(root.join("model").get("name"), model));
            if (minYear != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("year"), minYear));
            if (status != null && !status.trim().isEmpty())
                predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return vehicleRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getById(Integer id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    @Override
    public Vehicle createFromMap(Map<String, Object> payload) {
        requireComplete(payload);
        checkRefNoFree(str(payload, "refNo"), null);
        Vehicle vehicle = new Vehicle();
        vehicle.setIsNewArrival(true);   // a freshly added car starts as a New Arrival; admin can turn it off
        populateVehicle(vehicle, payload);
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle update(Integer id, Map<String, Object> payload) {
        Vehicle vehicle = getById(id);
        requireComplete(payload);
        checkRefNoFree(str(payload, "refNo"), id);
        populateVehicle(vehicle, payload);
        return vehicleRepository.save(vehicle);
    }

    // ---------- validation ----------
    private void requireComplete(Map<String, Object> p) {
        List<String> missing = new ArrayList<>();
        for (String[] field : REQUIRED_FIELDS) {
            Object value = p.get(field[0]);
            if (value == null || value.toString().trim().isEmpty()) {
                missing.add(field[1]);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Please fill in: " + String.join(", ", missing));
        }
    }

    private void checkRefNoFree(String refNo, Integer selfId) {
        vehicleRepository.findByRefNo(refNo).ifPresent(other -> {
            if (selfId == null || !other.getVehicleId().equals(selfId)) {
                throw new IllegalStateException("Ref No " + refNo + " is already used by another vehicle");
            }
        });
    }

    // ---------- payload helpers ----------
    private String str(Map<String, Object> p, String key) {
        Object o = p.get(key);
        return o == null ? null : o.toString().trim();
    }

    private Integer integer(Map<String, Object> p, String key) {
        String s = str(p, key);
        if (s == null || s.isEmpty()) return null;
        try {
            return new BigDecimal(s).intValue();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number for " + key);
        }
    }

    private void populateVehicle(Vehicle v, Map<String, Object> p) {
        if (p.containsKey("make") && p.containsKey("model")) {
            String makeName = str(p, "make");
            String modelName = str(p, "model");
            Make make = makeRepository.findAll().stream()
                    .filter(m -> m.getName().equalsIgnoreCase(makeName)).findFirst()
                    .orElseGet(() -> { Make nm = new Make(); nm.setName(makeName); return makeRepository.save(nm); });
            com.tradejapan.part2.model.Model model = modelRepository.findAll().stream()
                    .filter(m -> m.getName().equalsIgnoreCase(modelName) && m.getMake().getMakeId().equals(make.getMakeId()))
                    .findFirst()
                    .orElseGet(() -> {
                        com.tradejapan.part2.model.Model nm = new com.tradejapan.part2.model.Model();
                        nm.setName(modelName); nm.setMake(make); return modelRepository.save(nm);
                    });
            v.setModel(model);
        }

        if (p.containsKey("manufactureYear")) v.setYear(integer(p, "manufactureYear"));
        if (p.containsKey("fobPrice")) {
            try {
                v.setPriceUsd(new BigDecimal(str(p, "fobPrice")));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid FOB price");
            }
        }
        if (p.containsKey("description")) v.setDescription(str(p, "description"));

        // ---- the 18 specifications ----
        if (p.containsKey("refNo")) v.setRefNo(str(p, "refNo"));
        else if (v.getRefNo() == null) v.setRefNo("TJ" + System.currentTimeMillis());
        if (p.containsKey("category")) v.setBodyType(str(p, "category"));
        if (p.containsKey("vin")) v.setChassisNo(str(p, "vin"));
        if (p.containsKey("modelCode")) v.setModelCode(str(p, "modelCode"));
        if (p.containsKey("grade")) v.setGrade(str(p, "grade"));
        if (p.containsKey("engineType")) v.setEngineModel(str(p, "engineType"));
        if (p.containsKey("mileage")) v.setOdometerKm(integer(p, "mileage"));
        if (p.containsKey("displacementCc")) v.setDisplacementCc(integer(p, "displacementCc"));
        if (p.containsKey("steering")) v.setSteering(str(p, "steering"));
        if (p.containsKey("transmission")) v.setTransmission(str(p, "transmission"));
        if (p.containsKey("doors")) v.setDoors(integer(p, "doors"));
        if (p.containsKey("passengers")) v.setPassengers(integer(p, "passengers"));
        if (p.containsKey("dimension")) v.setDimension(str(p, "dimension"));
        if (p.containsKey("size")) v.setSize(str(p, "size"));
        if (p.containsKey("exteriorColor")) v.setExteriorColor(str(p, "exteriorColor"));
        if (p.containsKey("driveType")) v.setDriveType(str(p, "driveType"));
        if (p.containsKey("fuelType")) v.setFuelType(str(p, "fuelType"));
        if (p.containsKey("inventoryLocation")) v.setLocation(str(p, "inventoryLocation"));

        if (p.containsKey("condition")) {
            String condition = str(p, "condition").toUpperCase();
            if (!CONDITIONS.contains(condition)) {
                throw new IllegalArgumentException("Condition must be NEW or USED");
            }
            v.setVehicleCondition(condition);
        }

        if (p.containsKey("status")) {
            String status = str(p, "status").toUpperCase();
            if (!STATUSES.contains(status)) {
                throw new IllegalArgumentException("Status must be AVAILABLE, RESERVED or SOLD");
            }
            v.setStatus(status);
        }

        if (p.containsKey("imageUrls")) {
            String urls = str(p, "imageUrls");
            if (urls != null && !urls.isEmpty()) {
                v.getImages().clear();
                String[] urlArr = urls.split(",");
                int order = 0;
                for (String url : urlArr) {
                    if (url.trim().isEmpty()) continue;
                    VehicleImage vi = new VehicleImage();
                    vi.setImageUrl(url.trim());
                    vi.setVehicle(v);
                    vi.setIsMain(order == 0);
                    vi.setSortOrder(order + 1);
                    v.getImages().add(vi);
                    order++;
                }
            }
        }

        // ---- the 28 features (ON = id is in the list) ----
        if (p.containsKey("featureIds")) {
            v.getFeatures().clear();
            Object ids = p.get("featureIds");
            if (ids instanceof Collection) {
                for (Object o : (Collection<?>) ids) {
                    Integer featureId = Integer.valueOf(o.toString());
                    featureRepository.findById(featureId).ifPresent(v.getFeatures()::add);
                }
            }
        }
    }

    @Override public void delete(Integer id) { vehicleRepository.deleteById(id); }

    @Override
    public void setStatus(Integer id, String status) {
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(s)) {
            throw new IllegalArgumentException("Status must be AVAILABLE, RESERVED or SOLD");
        }
        Vehicle v = getById(id);
        v.setStatus(s);
        vehicleRepository.save(v);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getNewArrivals() {
        return vehicleRepository.findNewArrivals(org.springframework.data.domain.PageRequest.of(0, 8));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feature> getAllFeatures() {
        List<Feature> all = new ArrayList<>(featureRepository.findAll());
        // display in the catalogue order
        all.sort(Comparator.<Feature>comparingInt(f -> {
            int i = FeatureCatalog.NAMES.indexOf(f.getName());
            return i < 0 ? Integer.MAX_VALUE : i;
        }));
        return all;
    }

    @Override
    public Vehicle addImages(Integer id, List<String> imageUrls) {
        Vehicle v = getById(id);
        for (String url : imageUrls) {
            VehicleImage vi = new VehicleImage(); vi.setImageUrl(url); vi.setVehicle(v); v.getImages().add(vi);
        }
        return vehicleRepository.save(v);
    }

    @Override
    public Vehicle assignFeatures(Integer id, List<Integer> featureIds) {
        Vehicle v = getById(id);
        v.getFeatures().clear();
        for (Integer fId : featureIds) featureRepository.findById(fId).ifPresent(v.getFeatures()::add);
        return vehicleRepository.save(v);
    }

    @Override
    public void toggleNewArrival(Integer id) {
        Vehicle v = getById(id); v.setIsNewArrival(!v.getIsNewArrival()); vehicleRepository.save(v);
    }

    @Override
    public void deactivate(Integer id) {
        setActive(id, false);
    }

    @Override
    public void setActive(Integer id, boolean active) {
        Vehicle v = getById(id); v.setIsActive(active); vehicleRepository.save(v);
    }

    /** Every vehicle, including deactivated ones (admin panel only). */
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getAllForAdmin() {
        return vehicleRepository.findAll();
    }

    @Override
    public void removeImage(Integer id, Integer imageId) {
        Vehicle v = getById(id); v.getImages().removeIf(img -> img.getImageId().equals(imageId)); vehicleRepository.save(v);
    }

    // ---------- Wishlist ("Saved Vehicles") ----------
    @Override
    @Transactional
    public void saveToWishlist(Long userId, Integer vehicleId) {
        getById(vehicleId);   // 404 if the vehicle does not exist
        savedVehicleRepository.save(userId, vehicleId);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Integer vehicleId) {
        savedVehicleRepository.delete(userId, vehicleId);
    }

    /** Newest saved first. Vehicles an admin has deactivated are left out. */
    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getSavedVehicles(Long userId) {
        List<Integer> ids = savedVehicleRepository.findVehicleIds(userId);
        List<Vehicle> result = new ArrayList<>();
        if (ids.isEmpty()) return result;
        Map<Integer, Vehicle> byId = new HashMap<>();
        for (Vehicle v : vehicleRepository.findAllById(ids)) byId.put(v.getVehicleId(), v);
        for (Integer id : ids) {
            Vehicle v = byId.get(id);
            if (v != null && !Boolean.FALSE.equals(v.getIsActive())) result.add(v);
        }
        return result;
    }
}
