package com.example.mindLab.services;

import com.example.mindLab.models.ExperimentData;
import com.example.mindLab.repositories.ExperimentDataRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ExperimentDataService {

    private final ExperimentDataRepository experimentDataRepository;

    @Autowired
    public ExperimentDataService(ExperimentDataRepository experimentDataRepository) {
        this.experimentDataRepository = experimentDataRepository;
    }

    public List<ExperimentData> getAllExperimentData() {
        return experimentDataRepository.findAll();
    }



    public Optional<ExperimentData> getExperimentDataById(Long id) {
        return experimentDataRepository.findById(id);
    }

    public ExperimentData saveExperimentData(ExperimentData experimentData) {
        return experimentDataRepository.save(experimentData);
    }

    public List<ExperimentData> saveBatchExperimentData(List<ExperimentData> experimentDataList) {
        return experimentDataRepository.saveAll(experimentDataList);
    }

    public void deleteExperimentData(Long id) {
        experimentDataRepository.deleteById(id);
    }


    public List<ExperimentData> getExperimentDataByGroup(String groupe) {
        return experimentDataRepository.findByGroupe(groupe);
    }


    public List<ExperimentData> getExperimentDataByPatientGender(String gender) {
        return experimentDataRepository.findByPatientGender(gender);
    }

    public List<ExperimentData> getExperimentDataByPatientId(Long patientId) {
        List<ExperimentData> experimentDataList = experimentDataRepository.findByPatientId(patientId);


        return experimentDataList;
    }




    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void deleteDataByExperimentDataId(String experimentDataId) {
        // Find the ExperimentData entity by experimentDataId
        ExperimentData experimentData = entityManager.find(ExperimentData.class, experimentDataId);

        if (experimentData != null) {
            // Remove the associated reactionTimes
            experimentData.getReactionTimes().clear();
            experimentData.getId();

            // Remove the associated averageReactionTimes
            if (experimentData.getAverageReactionTime() != null) {
                entityManager.remove(experimentData.getAverageReactionTime());
                experimentData.setAverageReactionTime(null);
            }

            // Update the entity in the database
            entityManager.merge(experimentData);
        }
    }






}
