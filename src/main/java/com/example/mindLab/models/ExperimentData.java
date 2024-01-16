package com.example.mindLab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experiment_data")
public class ExperimentData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    private String experimentId;
    private String startSession;
    private String endSession;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "experiment_data_id")
    private List<ReactionTimes> reactionTimes;


    @OneToOne(cascade = CascadeType.ALL, mappedBy = "experimentData", orphanRemoval = true)
    @JsonIgnore
    private AverageReactionTimes averageReactionTime;


    public AverageReactionTimes getAverageReactionTimes() {
        return averageReactionTime;
    }

    public void setAverageReactionTimes(AverageReactionTimes averageReactionTime) {
        if (this.averageReactionTime == null || !this.averageReactionTime.equals(averageReactionTime)) {
            if (this.averageReactionTime != null) {
                this.averageReactionTime.setExperimentData(null);
            }
            this.averageReactionTime = averageReactionTime;
            if (averageReactionTime != null) {
                averageReactionTime.setExperimentData(this);
            }
        }
    }


    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }


    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "experiment_settings_id", nullable = false)
    @JsonIgnore
    private ExperimentSettings experimentSettings;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "patient_id")
    private Patient patient;

}