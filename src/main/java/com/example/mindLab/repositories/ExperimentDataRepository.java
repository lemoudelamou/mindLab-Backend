package com.example.mindLab.repositories;

import com.example.mindLab.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Collectors;

public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Long> {
    // You can add custom query methods if needed


}
