package com.tradeflow.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_images")
public class VehicleImage {
    private Integer imageId;
    private Vehicle vehicle;
    private String imageUrl;
    private Boolean isMain = false;
    private Integer sortOrder = 0;

    // Getters and Setters
    public Integer getImageId() { return imageId; }
    public void setImageId(Integer imageId) { this.imageId = imageId; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsMain() { return isMain; }
    public void setIsMain(Boolean isMain) { this.isMain = isMain; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
