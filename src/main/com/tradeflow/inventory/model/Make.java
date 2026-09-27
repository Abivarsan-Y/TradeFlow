package com.tradeflow.inventory.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "makes")
public class Make {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "make_id")
    private Integer makeId;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    // Getters and Setters
    public Integer getMakeId() { return makeId; }
    public void setMakeId(Integer makeId) { this.makeId = makeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
