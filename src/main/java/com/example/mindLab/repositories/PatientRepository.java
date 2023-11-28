package com.example.mindLab.repositories;

import com.example.mindLab.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    // You can add custom query methods if needed
}
