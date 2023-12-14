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
@RequestMapping("/api/settings/")  // Update the request mapping to include the patientId variable
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

    @GetMapping("/patient/{id}")
    public ResponseEntity<ExperimentSettings> getExperimentSettingsById(@PathVariable Long id) {
        Optional<ExperimentSettings> experimentSettings = experimentSettingsService.getExperimentSettingsById(id);
        return experimentSettings.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/save/{patientId}")
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

    @PutMapping("/update/{id}")
    public ResponseEntity<ExperimentSettings> updateExperimentSettings(
            @PathVariable Long id,
            @RequestBody ExperimentSettings updatedExperimentSettings) {
        Optional<ExperimentSettings> existingExperimentSettings = experimentSettingsService.getExperimentSettingsById(id);

        if (existingExperimentSettings.isPresent()) {
            // Retrieve the existing ExperimentSettings
            ExperimentSettings existingSettings = existingExperimentSettings.get();

            // Update only the fields you want to modify
            existingSettings.setExperiment(updatedExperimentSettings.getExperiment());
            existingSettings.setShape(updatedExperimentSettings.getShape());
            existingSettings.setExperimentLength(updatedExperimentSettings.getExperimentLength());
            existingSettings.setBlinkDelay(updatedExperimentSettings.getBlinkDelay());
            existingSettings.setIsColorBlind(updatedExperimentSettings.getIsColorBlind());
            existingSettings.setDifficultyLevel(updatedExperimentSettings.getDifficultyLevel());
            existingSettings.setColor1(updatedExperimentSettings.getColor1());
            existingSettings.setColor2(updatedExperimentSettings.getColor2());
            existingSettings.setColor3(updatedExperimentSettings.getColor3());

            // Save the updated ExperimentSettings
            ExperimentSettings savedExperimentSettings = experimentSettingsService.saveExperimentSettings(existingSettings);

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