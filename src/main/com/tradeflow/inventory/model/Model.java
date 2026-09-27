package com.tradeflow.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "models")
public class Model {

    private Integer modelId;
    private Make make;
    private String name;

    // Getters and Setters
    public Integer getModelId() { return modelId; }
    public void setModelId(Integer modelId) { this.modelId = modelId; }
    public Make getMake() { return make; }
    public void setMake(Make make) { this.make = make; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
