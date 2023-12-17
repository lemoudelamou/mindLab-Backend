package com.example.mindLab.repositories;

import com.example.mindLab.models.*;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Collectors;

public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Long> {
    // You can add custom query methods if needed


    @Query("SELECT ed FROM ExperimentData ed " +
            "JOIN ed.patient p " +
            "WHERE p.groupe = :group")
    List<ExperimentData> findByGroupe(@Param("group") String group);

    @Query("SELECT ed FROM ExperimentData ed " +
            "JOIN ed.experimentSettings es " +
            "JOIN es.patient p " +
            "WHERE p.gender = :gender")
    List<ExperimentData> findByPatientGender(@Param("gender") String gender);

    @Query("SELECT ed FROM ExperimentData ed " +
            "JOIN ed.experimentSettings es " +
            "JOIN es.patient p " +
            "WHERE p.id = :id")
    List<ExperimentData> findByPatientId(Long id);


    @Query("SELECT ed FROM ExperimentData ed " +
            "JOIN ed.experimentSettings es " +
            "JOIN es.patient p " +
            "WHERE p.fullname = :fullname AND p.id = :patientId")
    List<ExperimentData> findByPatientFullnameAndId(@Param("fullname") String fullname, @Param("patientId") Long patientId);





}
