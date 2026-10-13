package com.tradejapan.part2.repository;
import com.tradejapan.part2.model.Model;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ModelRepository extends JpaRepository<Model, Integer> {}
