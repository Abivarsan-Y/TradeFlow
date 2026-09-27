package com.tradeflow.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "features")
public class Feature {
    private Integer featureId;
    private String name;

    // Getters and Setters
    public Integer getFeatureId() { return featureId; }
    public void setFeatureId(Integer featureId) { this.featureId = featureId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
