package com.tradeflow.inventory.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    private Integer userId;
    private String firstName;
    private String email;
    private String role;

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
