package com.tradejapan.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "makes")
public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "make_id")
    private Integer makeId;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    public Integer getMakeId() { return makeId; }
    public void setMakeId(Integer makeId) { this.makeId = makeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
