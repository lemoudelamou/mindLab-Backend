package com.example.mindLab.controllers;

import com.example.mindLab.models.*;
import com.example.mindLab.services.ExperimentDataService;
import com.example.mindLab.services.ExperimentSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/settings/")
public class ExperimentDataController {

    private final ExperimentDataService experimentDataService;

    private final ExperimentSettingsService experimentSettingsService;


    public ExperimentSettings getExperimentSettingsById(Long experimentSettingsId) {
        return experimentSettingsService.getExperimentSettingsById(experimentSettingsId).orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Autowired
    public ExperimentDataController(ExperimentDataService experimentDataService, ExperimentSettingsService experimentSettingsService) {
        this.experimentDataService = experimentDataService;
        this.experimentSettingsService = experimentSettingsService;
    }

    @GetMapping
    public ResponseEntity<List<ExperimentData>> getAllExperimentData() {
        List<ExperimentData> experimentDataList = experimentDataService.getAllExperimentData();
        return new ResponseEntity<>(experimentDataList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperimentData> getExperimentDataById(@PathVariable Long id) {
        Optional<ExperimentData> experimentData = experimentDataService.getExperimentDataById(id);
        return experimentData.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
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

            if (averageReactionTimes == null || !averageReactionTimes.containsKey("positive") || !averageReactionTimes.containsKey("negative")) {
                return new ResponseEntity<>("Invalid request data: 'averageReactionTimes' is missing required fields", HttpStatus.BAD_REQUEST);
            }

            AverageReactionTimes avgReactionTime = new AverageReactionTimes();
            avgReactionTime.setNegative(((Number) averageReactionTimes.get("positive")).doubleValue());
            avgReactionTime.setNegative(((Number) averageReactionTimes.get("negative")).doubleValue());
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
}
