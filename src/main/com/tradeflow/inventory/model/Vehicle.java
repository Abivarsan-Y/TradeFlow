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

    private Integer vehicleId;
    private Model model;
    private Integer year;
    private BigDecimal priceUsd;
    private String refNo;
    private String bodyType;
    private String fuelType;
    private String steering;
    private String transmission;
    private String driveType;
    private String chassisNo;
    private String modelCode;
    private String grade;
    private String engineModel;
    private Integer odometerKm;
    private Integer displacementCc;
    private Integer doors;
    private Integer passengers;
    private String dimension;
    private String size;
    private String exteriorColor;
    private String location;
    private String description;
    private Boolean isNewArrival = false;
    private Boolean isActive = true;
    private String status = "AVAILABLE";
    private LocalDateTime createdAt;
    private List<VehicleImage> images = new ArrayList<>();

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
