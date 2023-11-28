package com.example.mindLab.repositories;

import com.example.mindLab.models.ExperimentData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Long> {
    // You can add custom query methods if needed
}
