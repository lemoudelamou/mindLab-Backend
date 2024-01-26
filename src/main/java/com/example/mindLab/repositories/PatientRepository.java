package com.example.mindLab.repositories;

import com.example.mindLab.models.ExperimentData;
import com.example.mindLab.models.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findAllByUser_Id(Long UserId);


}

