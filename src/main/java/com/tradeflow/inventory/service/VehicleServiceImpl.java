package com.tradeflow.inventory.service;

import com.tradeflow.inventory.model.*;
import com.tradeflow.inventory.repository.*;
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
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ModelRepository modelRepository;
    private final MakeRepository makeRepository;
    private final FeatureRepository featureRepository;
    private final VehicleImageRepository imageRepository;
    private final UserRepository userRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, ModelRepository modelRepository, MakeRepository makeRepository, FeatureRepository featureRepository, VehicleImageRepository imageRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.modelRepository = modelRepository;
        this.makeRepository = makeRepository;
        this.featureRepository = featureRepository;
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehicle> search(String make, String model, Integer minYear, String status, Pageable pageable) {
        Specification<Vehicle> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));

            if (make != null && !make.trim().isEmpty()) {
                predicates.add(cb.equal(root.join("model").join("make").get("name"), make));
            }
            if (model != null && !model.trim().isEmpty()) {
                predicates.add(cb.equal(root.join("model").get("name"), model));
            }
            if (minYear != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("year"), minYear));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
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
        Vehicle vehicle = new Vehicle();
        populateVehicle(vehicle, payload);
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle update(Integer id, Map<String, Object> payload) {
        Vehicle vehicle = getById(id);
        populateVehicle(vehicle, payload);
        return vehicleRepository.save(vehicle);
    }

    private void populateVehicle(Vehicle v, Map<String, Object> p) {
        if (p.containsKey("make") && p.containsKey("model")) {
            String makeName = (String) p.get("make");
            String modelName = (String) p.get("model");
            // Find or create make and model
            Make make = makeRepository.findAll().stream().filter(m -> m.getName().equalsIgnoreCase(makeName)).findFirst().orElseGet(() -> {
                Make nm = new Make(); nm.setName(makeName); return makeRepository.save(nm);
            });
            Model model = modelRepository.findAll().stream().filter(m -> m.getName().equalsIgnoreCase(modelName) && m.getMake().getMakeId().equals(make.getMakeId())).findFirst().orElseGet(() -> {
                Model nm = new Model(); nm.setName(modelName); nm.setMake(make); return modelRepository.save(nm);
            });
            v.setModel(model);
        }

        if (p.containsKey("manufactureYear")) v.setYear(Integer.valueOf(p.get("manufactureYear").toString()));
        if (p.containsKey("fobPrice")) v.setPriceUsd(new BigDecimal(p.get("fobPrice").toString()));
        if (p.containsKey("vin")) v.setChassisNo(p.get("vin").toString());
        if (p.containsKey("refNo")) v.setRefNo(p.get("refNo").toString());
        else if (v.getRefNo() == null) v.setRefNo("TJ" + System.currentTimeMillis()); // Generate ref no if missing

        if (p.containsKey("category")) v.setBodyType(p.get("category").toString());
        if (p.containsKey("engineType")) v.setEngineModel(p.get("engineType").toString());
        if (p.containsKey("mileage")) v.setOdometerKm(Integer.valueOf(p.get("mileage").toString()));
        if (p.containsKey("inventoryLocation")) v.setLocation(p.get("inventoryLocation").toString());
        if (p.containsKey("status")) v.setStatus(p.get("status").toString());

        if (p.containsKey("imageUrls")) {
            String urls = p.get("imageUrls").toString();
            if (urls != null && !urls.trim().isEmpty()) {
                v.getImages().clear();
                String[] urlArr = urls.split(",");
                for (int i = 0; i < urlArr.length; i++) {
                    VehicleImage vi = new VehicleImage();
                    vi.setImageUrl(urlArr[i].trim());
                    vi.setVehicle(v);
                    vi.setIsMain(i == 0);
                    v.getImages().add(vi);
                }
            }
        }
    }

    @Override
    public void delete(Integer id) {
        vehicleRepository.deleteById(id);
    }

    @Override
    public void setStatus(Integer id, String status) {
        Vehicle vehicle = getById(id);
        vehicle.setStatus(status);
        vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getNewArrivals() {
        return vehicleRepository.findTop8ByOrderByCreatedAtDesc();
    }

    @Override
    public Vehicle addImages(Integer id, List<String> imageUrls) {
        Vehicle v = getById(id);
        for (String url : imageUrls) {
            VehicleImage vi = new VehicleImage();
            vi.setImageUrl(url);
            vi.setVehicle(v);
            v.getImages().add(vi);
        }
        return vehicleRepository.save(v);
    }

    @Override
    public Vehicle assignFeatures(Integer id, List<Integer> featureIds) {
        Vehicle v = getById(id);
        v.getFeatures().clear();
        for (Integer fId : featureIds) {
            featureRepository.findById(fId).ifPresent(v.getFeatures()::add);
        }
        return vehicleRepository.save(v);
    }

    @Override
    public void toggleNewArrival(Integer id) {
        Vehicle v = getById(id);
        v.setIsNewArrival(!v.getIsNewArrival());
        vehicleRepository.save(v);
    }

    @Override
    public void deactivate(Integer id) {
        Vehicle v = getById(id);
        v.setIsActive(false);
        vehicleRepository.save(v);
    }

    @Override
    public void removeImage(Integer id, Integer imageId) {
        Vehicle v = getById(id);
        v.getImages().removeIf(img -> img.getImageId().equals(imageId));
        vehicleRepository.save(v);
    }

    @Override
    public void saveToWishlist(Integer userId, Integer vehicleId) {
        User user = userRepository.findById(userId).orElseThrow();
        Vehicle vehicle = getById(vehicleId);
        user.getSavedVehicles().add(vehicle);
        userRepository.save(user);
    }

    @Override
    public void removeFromWishlist(Integer userId, Integer vehicleId) {
        User user = userRepository.findById(userId).orElseThrow();
        Vehicle vehicle = getById(vehicleId);
        user.getSavedVehicles().remove(vehicle);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getSavedVehicles(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return new ArrayList<>(user.getSavedVehicles());
    }
}
