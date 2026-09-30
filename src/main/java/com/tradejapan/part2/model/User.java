package com.tradeflow.inventory.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String role;

    @ManyToMany
    @JoinTable(
        name = "saved_vehicles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "vehicle_id")
    )
    private Set<Vehicle> savedVehicles = new HashSet<>();

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Set<Vehicle> getSavedVehicles() { return savedVehicles; }
    public void setSavedVehicles(Set<Vehicle> savedVehicles) { this.savedVehicles = savedVehicles; }
}
