package com.tradeflow.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "models")
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Integer modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id", nullable = false)
    private Make make;

    @Column(nullable = false, length = 50)
    private String name;

    // Getters and Setters
    public Integer getModelId() { return modelId; }
    public void setModelId(Integer modelId) { this.modelId = modelId; }
    public Make getMake() { return make; }
    public void setMake(Make make) { this.make = make; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
