package com.tradeflow.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "features")
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feature_id")
    private Integer featureId;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    // Getters and Setters
    public Integer getFeatureId() { return featureId; }
    public void setFeatureId(Integer featureId) { this.featureId = featureId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
