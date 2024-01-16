package com.example.mindLab.controllers;

import com.example.mindLab.dto.ExperimentDetailsDTO;
import com.example.mindLab.models.*;
import com.example.mindLab.repositories.PatientRepository;
import com.example.mindLab.services.ExperimentDataService;
import com.example.mindLab.services.ExperimentSettingsService;
import com.example.mindLab.services.PatientService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;



@RestController
@RequestMapping("/api/data/")
public class ExperimentDataController {

    private final ExperimentDataService experimentDataService;

    private final ExperimentSettingsService experimentSettingsService;


    @Autowired
    private PatientService patientService;


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

    @GetMapping("/reaction-times-by-patient/{patientId}")
    public ResponseEntity<?> getReactionTimesByPatientId(@PathVariable Long patientId) {
        try {
            // Assuming there's a method like getExperimentDataByPatientId in your service
            List<ExperimentData> experimentDataList = experimentDataService.getExperimentDataByPatientId(patientId);

            if (!experimentDataList.isEmpty()) {
                // Combine reaction times from all experiment data associated with the patient
                List<ReactionTimes> reactionTimes = new ArrayList<>();
                for (ExperimentData experimentData : experimentDataList) {
                    reactionTimes.addAll(experimentData.getReactionTimes());
                }

                return new ResponseEntity<>(reactionTimes, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No experiment data found for the given patient", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/patient/{patientId}/{settingsId}/results/{experimentId}")
    public ResponseEntity<?> addExperimentDataWithSettings(
            @RequestBody Map<String, Object> requestData,
            @PathVariable Long settingsId,
            @PathVariable Long patientId,
            @PathVariable String experimentId
    ) {
        try {
            // Check if the required fields are present in the request data
            if (!requestData.containsKey("startSession") ||
                    !requestData.containsKey("endSession") ||
                    !requestData.containsKey("reactionTimes") ||
                    !requestData.containsKey("averageReactionTimes")) {
                return new ResponseEntity<>("Invalid request data", HttpStatus.BAD_REQUEST);
            }

            // Extract startSession and endSession from the request data
            String startSession = (String) requestData.get("startSession");
            String endSession = (String) requestData.get("endSession");

            // Validate or process startSession and endSession as needed

            ExperimentSettings experimentSettings = getExperimentSettingsById(settingsId);

            if (experimentSettings == null) {
                return new ResponseEntity<>("ExperimentSettings not found", HttpStatus.NOT_FOUND);
            }

            // Retrieve the patient by ID
            Optional<Patient> patientOptional = patientService.getPatientById(patientId);

            if (patientOptional.isEmpty()) {
                return new ResponseEntity<>("Patient not found", HttpStatus.NOT_FOUND);
            }

            Patient patient = patientOptional.get(); // Extract the patient from Optional

            ExperimentData experimentData = new ExperimentData();
            experimentData.setExperimentSettings(experimentSettings);
            experimentData.setPatient(patient); // Set the patient for the experimentData
            experimentData.setExperimentId(experimentId); // Set the experimentId
            experimentData.setStartSession(startSession); // Set the startSession
            experimentData.setEndSession(endSession); // Set the endSession

            // Extract reaction times from the request data
            Map<String, List<Map<String, Object>>> reactionTimesMap = (Map<String, List<Map<String, Object>>>) requestData.get("reactionTimes");
            List<Map<String, Object>> reactionTimesList = reactionTimesMap.get(experimentId);

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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteExperimentData(@PathVariable Long id) {
        Optional<ExperimentData> existingExperimentData = experimentDataService.getExperimentDataById(id);

        if (existingExperimentData.isPresent()) {
            experimentDataService.deleteExperimentData(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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


    @GetMapping("/gender/{gender}")
    public ResponseEntity<List<ExperimentData>> getExperimentDataByPatientGender(@PathVariable(required = false) String gender) {
        try {
            List<ExperimentData> experimentDataList;

            if (gender != null && !gender.equalsIgnoreCase("all")) {
                // Specific gender provided
                experimentDataList = experimentDataService.getExperimentDataByPatientGender(gender);
            } else {
                // No specific gender provided or "All" provided, fetch data for all genders
                experimentDataList = experimentDataService.getAllExperimentData();
            }

            if (experimentDataList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }



            return new ResponseEntity<>(experimentDataList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/data/patient/{patientId}")
    public ResponseEntity<?> getExperimentDataByPatientId(@PathVariable Long patientId) {
        try {
            List<ExperimentData> experimentDataList = experimentDataService.getExperimentDataByPatientId(patientId);

            if (!experimentDataList.isEmpty()) {
                // Map the data to the desired format
                List<Map<String, Object>> responseDataList = new ArrayList<>();

                for (ExperimentData experimentData : experimentDataList) {
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("experimentDataId", experimentData.getId()); // Add experimentDataId
                    responseData.put("experimentSettings", experimentData.getExperimentSettings());
                    responseData.put("patientInfo", experimentData.getPatient());
                    responseData.put("reactionTimes", experimentData.getReactionTimes());
                    responseData.put("averageReactionTimes", experimentData.getAverageReactionTimes());
                    responseDataList.add(responseData);
                }

                return ResponseEntity.ok(responseDataList);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }




    @GetMapping("/by-patient-fullname/{fullname}/by-patient-id/{patientId}")
    public ResponseEntity<?> getExperimentDataByPatientFullname(
            @PathVariable(required = false) String fullname,
            @PathVariable Long patientId) {
        try {
            // Decode the URL-encoded path variable
            if (fullname != null) {
                fullname = URLDecoder.decode(fullname, "UTF-8");
                // Manually trim the fullname to remove leading and trailing whitespaces
                fullname = fullname.trim();
            }

            List<ExperimentData> experimentDataList = experimentDataService.getExperimentDataByPatientFullnameAndId(fullname, patientId);

            if (!experimentDataList.isEmpty()) {
                // Map the data to the desired format
                List<Map<String, Object>> responseDataList = new ArrayList<>();

                for (ExperimentData experimentData : experimentDataList) {
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("patient", experimentData.getPatient());
                    responseData.put("experimentDataId", experimentData.getId()); // Add experimentDataId
                    responseData.put("experimentSettings", experimentData.getExperimentSettings());
                    responseData.put("reactionTimes", experimentData.getReactionTimes());
                    responseData.put("averageReactionTimes", experimentData.getAverageReactionTimes());
                    responseDataList.add(responseData);
                }

                return ResponseEntity.ok(responseDataList);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error decoding path variable");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }



    @DeleteMapping("/delete-reaction-times/{id}")
    public ResponseEntity<String> deleteReactionTimesByExperimentId(@PathVariable String id) {
        try {
            experimentDataService.deleteDataByExperimentDataId(id);
            return new ResponseEntity<>("Reaction times deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting reaction times: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
