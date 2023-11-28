package com.example.mindLab.repositories;

import com.example.mindLab.models.ExperimentSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentSettingsRepository extends JpaRepository<ExperimentSettings, Long> {
    // You can add custom query methods if needed
}
