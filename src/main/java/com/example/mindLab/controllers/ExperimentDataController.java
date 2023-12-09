package com.example.mindLab.controllers;

import com.example.mindLab.dto.ExperimentDetailsDTO;
import com.example.mindLab.models.*;
import com.example.mindLab.repositories.PatientRepository;
import com.example.mindLab.services.ExperimentDataService;
import com.example.mindLab.services.ExperimentSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/data/")
public class ExperimentDataController {

    private final ExperimentDataService experimentDataService;

    private final ExperimentSettingsService experimentSettingsService;


    public ExperimentSettings getExperimentSettingsById(Long experimentSettingsId) {
        return experimentSettingsService.getExperimentSettingsById(experimentSettingsId).orElseThrow(() -> new RuntimeException("Patient not found"));
    }


    private final PatientRepository patientRepository;


    @Autowired
    public ExperimentDataController(ExperimentDataService experimentDataService, ExperimentSettingsService experimentSettingsService, PatientRepository patientRepository) {
        this.experimentDataService = experimentDataService;
        this.experimentSettingsService = experimentSettingsService;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExperimentData>> getAllExperimentData() {
        List<ExperimentData> experimentDataList = experimentDataService.getAllExperimentData();
        return new ResponseEntity<>(experimentDataList, HttpStatus.OK);
    }

    @GetMapping("/reaction-times/{experimentSettingsId}")
    public ResponseEntity<?> getReactionTimesByExperimentDataId(@PathVariable Long experimentDataId) {
        try {
            Optional<ExperimentData> experimentData = experimentDataService.getExperimentDataById(experimentDataId);

            if (experimentData.isPresent()) {
                List<ReactionTimes> reactionTimes = experimentData.get().getReactionTimes();
                return new ResponseEntity<>(reactionTimes, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("ExperimentData not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/{settingsId}/results")
    public ResponseEntity<?> addExperimentDataWithSettings(
            @RequestBody Map<String, Object> requestData,
            @PathVariable Long settingsId
    ) {
        try {
            // Check if the required fields are present in the request data
            if (!requestData.containsKey("reactionTimes") || !requestData.containsKey("averageReactionTimes")) {
                return new ResponseEntity<>("Invalid request data", HttpStatus.BAD_REQUEST);
            }

            ExperimentSettings experimentSettings = getExperimentSettingsById(settingsId);

            if (experimentSettings == null) {
                return new ResponseEntity<>("ExperimentSettings not found", HttpStatus.NOT_FOUND);
            }

            ExperimentData experimentData = new ExperimentData();
            experimentData.setExperimentSettings(experimentSettings);

            // Extract reaction times from the request data
            List<Map<String, Object>> reactionTimesList = (List<Map<String, Object>>) requestData.get("reactionTimes");

            if (reactionTimesList == null) {
                return new ResponseEntity<>("Invalid request data: 'reactionTimes' is missing", HttpStatus.BAD_REQUEST);
            }

            List<ReactionTimes> reactionTimes = new ArrayList<>();
            for (Map<String, Object> reaction : reactionTimesList) {
                if (reaction.containsKey("time") && reaction.containsKey("status")) {
                    ReactionTimes rt = new ReactionTimes();
                    rt.setTime(((Number) reaction.get("time")).doubleValue());
                    rt.setStatus((String) reaction.get("status"));
                    reactionTimes.add(rt);
                } else {
                    return new ResponseEntity<>("Invalid reaction data", HttpStatus.BAD_REQUEST);
                }
            }
            experimentData.setReactionTimes(reactionTimes);

            // Extract average reaction times from the request data
            Map<String, Double> averageReactionTimes = (Map<String, Double>) requestData.get("averageReactionTimes");

            if (averageReactionTimes == null || !averageReactionTimes.containsKey("correct") || !averageReactionTimes.containsKey("incorrect")) {
                return new ResponseEntity<>("Invalid request data: 'averageReactionTimes' is missing required fields", HttpStatus.BAD_REQUEST);
            }

            AverageReactionTimes avgReactionTime = new AverageReactionTimes();
            avgReactionTime.setCorrect(((Number) averageReactionTimes.get("correct")).doubleValue());
            avgReactionTime.setIncorrect(((Number) averageReactionTimes.get("incorrect")).doubleValue());
            experimentData.setAverageReactionTimes(avgReactionTime);

            ExperimentData newExperimentData = experimentDataService.saveExperimentData(experimentData);

            return new ResponseEntity<>(newExperimentData, HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the exception and provide a generic error message
            e.printStackTrace();
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PutMapping("/{id}")
    public ResponseEntity<ExperimentData> updateExperimentData(
            @PathVariable Long id,
            @RequestBody ExperimentData updatedExperimentData
    ) {
        Optional<ExperimentData> existingExperimentData = experimentDataService.getExperimentDataById(id);

        if (existingExperimentData.isPresent()) {
            updatedExperimentData.setId(id);
            ExperimentData savedExperimentData = experimentDataService.saveExperimentData(updatedExperimentData);
            return new ResponseEntity<>(savedExperimentData, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExperimentData(@PathVariable Long id) {
        experimentDataService.deleteExperimentData(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }




    @GetMapping("/average-reaction-times/{experimentDataId}")
    public ResponseEntity<?> getAverageReactionTimesByExperimentDataId(@PathVariable Long experimentDataId) {
        try {
            Optional<ExperimentData> experimentData = experimentDataService.getExperimentDataById(experimentDataId);

            if (experimentData.isPresent()) {
                AverageReactionTimes avgReactionTimes = experimentData.get().getAverageReactionTimes();
                if (avgReactionTimes != null) {
                    return ResponseEntity.ok(avgReactionTimes);
                } else {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("AverageReactionTimes not available");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ExperimentData not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


    @GetMapping("/experiment-details/{experimentDataId}")
    public ResponseEntity<?> getExperimentDetailsById(@PathVariable Long experimentDataId) {
        try {
            Optional<ExperimentData> experimentDataOptional = experimentDataService.getExperimentDataById(experimentDataId);

            if (experimentDataOptional.isPresent()) {
                ExperimentDetailsDTO experimentDetails = getExperimentDetailsDTO(experimentDataOptional);

                return ResponseEntity.ok(experimentDetails);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ExperimentData not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    private static ExperimentDetailsDTO getExperimentDetailsDTO(Optional<ExperimentData> experimentDataOptional) {
        ExperimentData experimentData = experimentDataOptional.get();


        // Create DTO
        ExperimentDetailsDTO experimentDetails = new ExperimentDetailsDTO();
        experimentDetails.setPatient(experimentData.getExperimentSettings().getPatient());
        experimentDetails.setExperimentSettings(experimentData.getExperimentSettings());
        experimentDetails.setReactionTimes(experimentData.getReactionTimes());
        experimentDetails.setAverageReactionTimes(experimentData.getAverageReactionTimes());
        return experimentDetails;
    }

    @GetMapping("/group/{groupe}")
    public ResponseEntity<List<ExperimentData>> getExperimentDataByGroup(@PathVariable String groupe) {
        try {
            List<ExperimentData> experimentDataList = experimentDataService.getExperimentDataByGroup(groupe);

            if (experimentDataList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(experimentDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
