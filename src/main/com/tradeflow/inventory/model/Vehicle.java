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

}
