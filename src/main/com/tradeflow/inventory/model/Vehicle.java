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

}
