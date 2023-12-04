package com.example.mindLab.services;

import com.example.mindLab.models.ExperimentData;
import com.example.mindLab.repositories.ExperimentDataRepository;
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



}
