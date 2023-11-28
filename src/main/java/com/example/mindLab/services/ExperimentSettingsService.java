package com.example.mindLab.services;

import com.example.mindLab.models.ExperimentSettings;
import com.example.mindLab.repositories.ExperimentSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExperimentSettingsService {

    private final ExperimentSettingsRepository experimentSettingsRepository;

    @Autowired
    public ExperimentSettingsService(ExperimentSettingsRepository experimentSettingsRepository) {
        this.experimentSettingsRepository = experimentSettingsRepository;
    }

    public List<ExperimentSettings> getAllExperimentSettings() {
        return experimentSettingsRepository.findAll();
    }

    public Optional<ExperimentSettings> getExperimentSettingsById(Long id) {
        return experimentSettingsRepository.findById(id);
    }

    public ExperimentSettings saveExperimentSettings(ExperimentSettings experimentSettings) {
        return experimentSettingsRepository.save(experimentSettings);
    }

    public void deleteExperimentSettings(Long id) {
        experimentSettingsRepository.deleteById(id);
    }
}
