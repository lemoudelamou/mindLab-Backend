package com.example.mindLab.services;

import com.example.mindLab.models.*;
import com.example.mindLab.repositories.ExperimentDataRepository;
import com.example.mindLab.repositories.ExperimentSettingsRepository;
import com.example.mindLab.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository, ExperimentSettingsRepository experimentSettingsRepository, ExperimentDataRepository experimentDataRepository) {
        this.patientRepository = patientRepository;

    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }


    public List<Patient> getAllPatientsForDoctor(Long userId) {
        return patientRepository.findAllByUser_Id(userId);
    }


}

