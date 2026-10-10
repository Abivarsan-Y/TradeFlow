package com.tradeflow.inventory.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    @JsonProperty("id")
    private Integer vehicleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "model_id", nullable = false)
    @JsonIgnore
    private Model model;

    @Transient
    @JsonProperty("make")
    public String getMakeName() {
        return model != null && model.getMake() != null ? model.getMake().getName() : null;
    }

    @Transient
    @JsonProperty("model")
    public String getModelName() {
        return model != null ? model.getName() : null;
    }

    @Column(nullable = false)
    @JsonProperty("manufactureYear")
    private Integer year;

    @Column(name = "price_usd", nullable = false, precision = 10, scale = 2)
    @JsonProperty("fobPrice")
    private BigDecimal priceUsd;

    @Column(name = "ref_no", unique = true, nullable = false, length = 30)
    private String refNo;

    @Column(name = "body_type", length = 50)
    @JsonProperty("category")
    private String bodyType;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(length = 50)
    private String steering;

    @Column(length = 50)
    private String transmission;

    @Column(name = "drive_type", length = 50)
    private String driveType;

    @Column(name = "chassis_no", length = 50)
    @JsonProperty("vin")
    private String chassisNo;

    @Column(name = "model_code", length = 50)
    private String modelCode;

    @Column(length = 50)
    private String grade;

    @Column(name = "engine_model", length = 50)
    @JsonProperty("engineType")
    private String engineModel;

    @Column(name = "odometer_km", nullable = false)
    @JsonProperty("mileage")
    private Integer odometerKm;

    @Column(name = "displacement_cc")
    private Integer displacementCc;

    private Integer doors;
    private Integer passengers;

    @Column(length = 50)
    private String dimension;

    @Column(length = 50)
    private String size;

    @Column(name = "exterior_color", length = 30)
    private String exteriorColor;

    @Column(length = 50)
    @JsonProperty("inventoryLocation")
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_new_arrival")
    private Boolean isNewArrival = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(length = 20)
    private String status = "AVAILABLE";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<VehicleImage> images = new ArrayList<>();

    @Transient
    @JsonProperty("imageUrls")
    public String getImageUrlsString() {
        if (images == null || images.isEmpty()) return null;
        List<String> urls = new ArrayList<>();
        for (VehicleImage img : images) {
            urls.add(img.getImageUrl());
        }
        return String.join(",", urls);
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "vehicle_features",
        joinColumns = @JoinColumn(name = "vehicle_id"),
        inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private Set<Feature> features = new HashSet<>();

    // Basic Getters and Setters
    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }
    public Model getModel() { return model; }
    public void setModel(Model model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }
    public String getRefNo() { return refNo; }
    public void setRefNo(String refNo) { this.refNo = refNo; }
    public String getBodyType() { return bodyType; }
    public void setBodyType(String bodyType) { this.bodyType = bodyType; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getSteering() { return steering; }
    public void setSteering(String steering) { this.steering = steering; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public String getDriveType() { return driveType; }
    public void setDriveType(String driveType) { this.driveType = driveType; }
    public String getChassisNo() { return chassisNo; }
    public void setChassisNo(String chassisNo) { this.chassisNo = chassisNo; }
    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getEngineModel() { return engineModel; }
    public void setEngineModel(String engineModel) { this.engineModel = engineModel; }
    public Integer getOdometerKm() { return odometerKm; }
    public void setOdometerKm(Integer odometerKm) { this.odometerKm = odometerKm; }
    public Integer getDisplacementCc() { return displacementCc; }
    public void setDisplacementCc(Integer displacementCc) { this.displacementCc = displacementCc; }
    public Integer getDoors() { return doors; }
    public void setDoors(Integer doors) { this.doors = doors; }
    public Integer getPassengers() { return passengers; }
    public void setPassengers(Integer passengers) { this.passengers = passengers; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getExteriorColor() { return exteriorColor; }
    public void setExteriorColor(String exteriorColor) { this.exteriorColor = exteriorColor; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsNewArrival() { return isNewArrival; }
    public void setIsNewArrival(Boolean isNewArrival) { this.isNewArrival = isNewArrival; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<VehicleImage> getImages() { return images; }
    public void setImages(List<VehicleImage> images) { this.images = images; }
    public Set<Feature> getFeatures() { return features; }
    public void setFeatures(Set<Feature> features) { this.features = features; }
}
