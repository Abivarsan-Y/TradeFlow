package com.tradeflow.inventory.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "makes")
public class Make {
    private Integer makeId;

    private String name;

    // Getters and Setters
    public Integer getMakeId() { return makeId; }
    public void setMakeId(Integer makeId) { this.makeId = makeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
