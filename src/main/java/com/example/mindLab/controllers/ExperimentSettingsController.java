package com.example.mindLab.controllers;

import com.example.mindLab.models.ExperimentSettings;
import com.example.mindLab.models.Patient;
import com.example.mindLab.services.ExperimentSettingsService;
import com.example.mindLab.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients/{patientId}/settings")  // Update the request mapping to include the patientId variable
public class ExperimentSettingsController {

    private final ExperimentSettingsService experimentSettingsService;

    @Autowired
    private PatientService patientService;

    @Autowired
    public ExperimentSettingsController(ExperimentSettingsService experimentSettingsService) {
        this.experimentSettingsService = experimentSettingsService;
    }

    public Patient getPatientById(Long patientId) {
        return patientService.getPatientById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @GetMapping
    public ResponseEntity<List<ExperimentSettings>> getAllExperimentSettings() {
        List<ExperimentSettings> experimentSettingsList = experimentSettingsService.getAllExperimentSettings();
        return new ResponseEntity<>(experimentSettingsList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperimentSettings> getExperimentSettingsById(@PathVariable Long id) {
        Optional<ExperimentSettings> experimentSettings = experimentSettingsService.getExperimentSettingsById(id);
        return experimentSettings.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ExperimentSettings> addExperimentSettings(
            @PathVariable Long patientId,  // Include the patientId variable in the path
            @RequestBody ExperimentSettings experimentSettings) {
        try {
            // Fetch the patient from the database
            Patient patient = getPatientById(patientId);

            // Set the patient for the ExperimentSettings
            experimentSettings.setPatient(patient);

            // Save the ExperimentSettings
            ExperimentSettings newExperimentSettings = experimentSettingsService.saveExperimentSettings(experimentSettings);

            return new ResponseEntity<>(newExperimentSettings, HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the exception or handle it as needed
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperimentSettings> updateExperimentSettings(
            @PathVariable Long patientId,
            @PathVariable Long id,
            @RequestBody ExperimentSettings updatedExperimentSettings) {
        Optional<ExperimentSettings> existingExperimentSettings = experimentSettingsService.getExperimentSettingsById(id);

        if (existingExperimentSettings.isPresent()) {
            updatedExperimentSettings.setId(id);
            ExperimentSettings savedExperimentSettings = experimentSettingsService.saveExperimentSettings(updatedExperimentSettings);
            return new ResponseEntity<>(savedExperimentSettings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExperimentSettings(@PathVariable Long id) {
        experimentSettingsService.deleteExperimentSettings(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
