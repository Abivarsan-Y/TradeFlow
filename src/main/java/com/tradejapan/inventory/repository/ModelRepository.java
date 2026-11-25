package com.tradejapan.inventory.repository;
import com.tradejapan.inventory.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ModelRepository extends JpaRepository<Model, Integer> {}
